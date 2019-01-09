package jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javassist.CannotCompileException;
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
    this.jarClasses = getClassesFromJar();
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

  private ArrayList<JarClass> getClassesFromJar()
      throws IOException, NotFoundException {

    JarFile jarFile = new JarFile(pathToJar);
    manifest = jarFile.getManifest();

    Enumeration<JarEntry> e = jarFile.entries();
    ArrayList<JarClass> classes = new ArrayList<>();

    while (e.hasMoreElements()) {
      JarEntry je = e.nextElement();
      if (je.isDirectory() || !je.getName().endsWith(".class")) {
        continue;
      }
      String className = je.getName().substring(0, je.getName().length() - 6);
      className = className.replace('/', '.');

      classes.add(new JarClass(cp.get(className)));
    }

    return classes;
  }

  public void extractJar(String destDir) throws IOException {

    FileUtils.deleteDirectory(new File(destDir));
    FileUtils.forceMkdir(new File(destDir));

    JarFile jar = new JarFile(pathToJar);
    manifest = jar.getManifest();

    Enumeration enumEntries = jar.entries();

    while (enumEntries.hasMoreElements()) {
      JarEntry file = (JarEntry) enumEntries.nextElement();

//      String classPath = file.getName().substring(0, file.getName().length() - 5);
//      classPath = classPath.replace(".", "/");

      File f = new File(destDir + File.separator + file.getName());

      if (file.isDirectory()) {
        f.mkdir();
        continue;
      }

      InputStream is = jar.getInputStream(file);
      FileOutputStream fos = new FileOutputStream(f);

      while (is.available() > 0) {
        fos.write(is.read());
      }

      fos.close();
      is.close();
    }
    jar.close();
  }

  public void removeClass(JarClass jarClass) throws IOException, NotFoundException {

    CtClass ctClass = jarClass.getCtClass();

    JarFile jarFile = new JarFile(pathToJar);
    Enumeration<JarEntry> e = jarFile.entries();

    List<JarEntry> newEntries = new ArrayList<>();

    while (e.hasMoreElements()) {
      JarEntry je = e.nextElement();
      if (je.isDirectory() || !je.getName().endsWith(".class")) {
        continue;
      }
      String className = je.getName().substring(0, je.getName().length() - 6);
      className = className.replace('/', '.');

      if (!cp.get(className).equals(ctClass)) {
        newEntries.add(je);
      }
    }

    FileOutputStream fos = new FileOutputStream(pathToJar + ".tmp");
    JarOutputStream jos = new JarOutputStream(fos, jarFile.getManifest());

    for (JarEntry entry : newEntries) {
      jos.putNextEntry(entry);
    }

    jos.close();
    fos.close();

    File old = new File(pathToJar);
    old.delete();
    File newJar = new File(pathToJar + ".tmp");
    newJar.renameTo(old);
  }

  public void saveClass(CtClass ctClass)
      throws IOException, CannotCompileException {

    cp.removeClassPath(path);
    ctClass.toClass(this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
    replaceJarFile(pathToJar, ctClass.toBytecode(), ctClass.getName() + ".class");
    cp.appendClassPath(path);
  }

  private void replaceJarFile(String jarPathAndName, byte[] fileByteCode, String fileName)
      throws IOException {

    File jarFile = new File(jarPathAndName);
    File tempJarFile = new File("./tmp.jar");
    JarFile jar = new JarFile(jarFile);
    boolean jarWasUpdated = false;

    try {
      JarOutputStream tempJar =
          new JarOutputStream(new FileOutputStream(tempJarFile));

      byte[] buffer = new byte[1024];
      int bytesRead;

      try {
        try {
          JarEntry entry = new JarEntry(fileName);
          tempJar.putNextEntry(entry);
          tempJar.write(fileByteCode);

        } catch (Exception ex) {
          System.out.println(ex);
          tempJar.putNextEntry(new JarEntry("stub"));
        }

        InputStream entryStream = null;
        for (Enumeration entries = jar.entries(); entries.hasMoreElements(); ) {

          JarEntry entry = (JarEntry) entries.nextElement();

          if (!entry.getName().equals(fileName)) {

            entryStream = jar.getInputStream(entry);
            tempJar.putNextEntry(entry);

            while ((bytesRead = entryStream.read(buffer)) != -1) {
              tempJar.write(buffer, 0, bytesRead);
            }
          }
        }
        if (entryStream != null) {
          entryStream.close();
        }
        jarWasUpdated = true;
      } catch (Exception ex) {
        System.out.println(ex);
        tempJar.putNextEntry(new JarEntry("stub"));

      } finally {
        tempJar.close();
      }
    } finally {

      jar.close();

      if (!jarWasUpdated) {
        tempJarFile.delete();
      }
    }

    if (jarWasUpdated) {
      if (jarFile.delete()) {
        tempJarFile.renameTo(jarFile);
        System.out.println(jarPathAndName + " updated.");
      } else {
        System.out.println("Could Not Delete JAR File");
      }
    }
  }

  public void update() throws IOException, NotFoundException {
    this.jarClasses = getClassesFromJar();
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
          .filter(s -> s.toString().endsWith(".class"))
          .map(s -> s.toString().substring(EXTRACT_PATH.length() + 1)
              .replace('/', '.'))
          .forEach(classes::add);

    } catch (IOException e) {
      e.printStackTrace();
    }

    return classes;
  }
}
