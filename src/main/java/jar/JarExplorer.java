package jar;

import java.io.File;
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
import java.util.zip.ZipOutputStream;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import org.apache.commons.io.FileUtils;

public class JarExplorer {

  private String pathToJar;
  private ClassPool cp;
  private ClassPath path;
  private List<JarClass> jarClasses;
  private List<JarClass> editedJarClasses = new ArrayList<>();
  private Manifest manifest;
  private final String EXTRACT_PATH = "./extracted";

  public JarExplorer(String pathToJar) throws NotFoundException, IOException {
    this.pathToJar = pathToJar;
    cp = ClassPool.getDefault();
    path = cp.insertClassPath(pathToJar);
    ClassPool.doPruning = false;
    manifest = getManifest();

    extractArchive(Paths.get(pathToJar), Paths.get(EXTRACT_PATH));
    List<String> classNames = getClassNames(Paths.get(EXTRACT_PATH));

    jarClasses = new ArrayList<>();
    for (String s : classNames) {
      jarClasses.add(new JarClass(cp.get(s)));
    }
  }

  public void addEditedJarClass(JarClass clazz) {
    editedJarClasses.add(clazz);
  }

  public List<JarClass> getJarClasses() {
    return jarClasses;
  }

  private Manifest getManifest() throws IOException {
    return new JarFile(pathToJar).getManifest();
  }

  private void extractArchive(Path archiveFile, Path destPath) throws IOException {

    FileUtils.deleteDirectory(destPath.toFile());
    Files.createDirectories(destPath);

    try (ZipFile archive = new ZipFile(archiveFile.toFile())) {

      List<? extends ZipEntry> entries = archive.stream()
          .sorted(Comparator.comparing(ZipEntry::getName))
          .collect(Collectors.toList());

      for (ZipEntry entry : entries) {
        Path entryDest = destPath.resolve(entry.getName());

        System.out.println(entry.getName());

        if (entry.isDirectory() && !entry.getName().endsWith("//")) {
          Files.createDirectory(entryDest);
          continue;
        }

        if (!entry.getName().endsWith("//")) {
          Files.copy(archive.getInputStream(entry), entryDest);
        }
      }
    }
  }

  private void compileClasses() {
    for (JarClass clazz : editedJarClasses) {
      System.out.println("Compilling " + clazz.getName());
      saveClass(clazz.getCtClass());
    }
  }

  public void saveJar(String pathToJar) throws IOException {
    compileClasses();
    jarDirectory(EXTRACT_PATH, pathToJar);
    FileUtils.deleteDirectory(new File(EXTRACT_PATH));
  }

  private List<String> getClassNames(Path path) {

    ArrayList<String> classes = new ArrayList<>();

    try (Stream<Path> paths = Files.walk(path)) {

      paths
          .filter(Files::isRegularFile)
          .map(file -> file.toString().substring(EXTRACT_PATH.length() + 1))
          .filter(f -> f.endsWith(".class"))
          .map(s -> s.replace("/", "."))
          .map(s -> s.substring(0, s.length() - 6))
          .forEach(classes::add);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return classes;
  }

  private void saveClass(CtClass ctClass) {
    try {
      ctClass.toClass(this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
      System.out.println(ctClass.getName());
      ctClass.writeFile(EXTRACT_PATH);
    } catch (IOException | CannotCompileException e) {
      e.printStackTrace();
    }
  }

  private static void jarDirectory(String sourceDirectoryPath, String jarPath) throws IOException {
    Path zipFilePath = Files.createFile(Paths.get(jarPath));

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(
        Files.newOutputStream(zipFilePath))) {

      Path sourceDirPath = Paths.get(sourceDirectoryPath);

      Files.walk(sourceDirPath).filter(path -> !Files.isDirectory(path))
          .forEach(path -> {
            ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
            try {
              zipOutputStream.putNextEntry(zipEntry);
              zipOutputStream.write(Files.readAllBytes(path));
              zipOutputStream.closeEntry();
            } catch (Exception e) {
              System.err.println(e);
            }
          });
    }
  }

  public static int getMod(String[] mods) {
    int mod = 0;
    for (String mod1 : mods) {
      switch (mod1) {
        case "public": {
          mod += Modifier.PUBLIC;
          break;
        }
        case "protected": {
          mod += Modifier.PROTECTED;
          break;
        }
        case "private": {
          mod += Modifier.PRIVATE;
          break;
        }
        case "static": {
          mod += Modifier.STATIC;
          break;
        }
        case "final": {
          mod += Modifier.FINAL;
          break;
        }
        case "static final": {
          mod += Modifier.STATIC;
          mod += Modifier.FINAL;
          break;
        }
        case "interface": {
          mod += Modifier.INTERFACE;
          break;
        }
        case "abstract": {
          mod += Modifier.ABSTRACT;
          break;
        }
        default: {
          mod += 0;
          break;
        }
      }
    }
    return mod;
  }

  public CtClass makeClass(String result) {
    return cp.makeClass(result);
  }

  public CtClass getInterface(String body) {
    try {
      return cp.get(body);
    } catch (NotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }
}
