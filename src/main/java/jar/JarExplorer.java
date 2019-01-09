package jar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.commons.io.FileUtils;

public class JarExplorer {

  private String pathToJar;
  private ClassPool cp;
  private ClassPath path;
  private List<JarClass> jarClasses;
  private Manifest manifest;
  private final String EXTRACT_PATH = "./extracted";

  public JarExplorer(String pathToJar) throws NotFoundException, IOException {
    this.pathToJar = pathToJar;
    cp = ClassPool.getDefault();
    path = cp.insertClassPath(pathToJar);
    ClassPool.doPruning = false;
//    this.jarClasses = getClassesFromJar();

    extractArchive(Paths.get(pathToJar), Paths.get(EXTRACT_PATH));

    List<String> classNames = getClassNames(Paths.get(EXTRACT_PATH));

    jarClasses = new ArrayList<>();

    for (String s : classNames) {
      System.out.println(s);
      jarClasses.add(new JarClass(cp.get(s)));
    }

  }

  public List<JarClass> getJarClasses() {
    return jarClasses;
  }

  public Manifest getManifest() throws IOException {
    return new JarFile(pathToJar).getManifest();
  }

  public List<JarClass> getJarClassesFromNames(List<String> classNames) throws NotFoundException {

    List<JarClass> classes = new ArrayList<>();

    for (String s : classNames) {
      classes.add(new JarClass(cp.get(s)));
    }

    return classes;
  }


  public void extractArchive(Path archiveFile, Path destPath) throws IOException {

    FileUtils.deleteDirectory(destPath.toFile());
    Files.createDirectories(destPath); // create dest path folder(s)

    try (ZipFile archive = new ZipFile(archiveFile.toFile())) {

      // sort entries by name to always create folders first
      List<? extends ZipEntry> entries = archive.stream()
          .sorted(Comparator.comparing(ZipEntry::getName))
          .collect(Collectors.toList());

      // copy each entry in the dest path
      for (ZipEntry entry : entries) {
        Path entryDest = destPath.resolve(entry.getName());

        if (entry.isDirectory()) {
          Files.createDirectory(entryDest);
          continue;
        }

        Files.copy(archive.getInputStream(entry), entryDest);
      }
    }
  }

  public List<String> getClassNames(Path path) {

    ArrayList<String> classes = new ArrayList<>();

    try (Stream<Path> paths = Files.walk(path)) {

      paths
          .filter(Files::isRegularFile)
          .map(file -> file.toString().substring(EXTRACT_PATH.length() + 1))
          .filter(f -> f.endsWith(".class"))
          .map(s -> s.replace("/", "."))
          .forEach(classes::add);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return classes;
  }

  public void saveClass(CtClass ctClass) {

  }
}
