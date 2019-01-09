package jar;

import javassist.CtClass;

public class JarClass {

  private String name;
  private CtClass ctClass;

  public JarClass(CtClass ctClass) {
    this.ctClass = ctClass;
    this.name = ctClass.getName();
  }

  public String getName() {
    return name;
  }

  public CtClass getCtClass() {
    return ctClass;
  }

  @Override
  public String toString() {
    return name;
  }
}
