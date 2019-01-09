package jar;

import javassist.CtClass;
import javassist.CtMethod;

public class JarMethod {

  private CtClass ctClass;
  private CtMethod ctMethod;
  private String name;

  public JarMethod(CtClass ctClass, CtMethod method) {
    this.ctClass = ctClass;
    this.ctMethod = method;
    this.name = method.getLongName();
  }

  public CtMethod getCtMethod() {
    return ctMethod;
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
