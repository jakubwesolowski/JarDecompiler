package jar;

import javassist.CtClass;
import javassist.CtConstructor;

public class JarConstructor {

  private CtClass ctClass;
  private CtConstructor ctConstructor;
  private String name;

  public JarConstructor(CtClass ctClass, CtConstructor constructor) {
    this.ctClass = ctClass;
    this.ctConstructor = constructor;
    this.name = constructor.getLongName();
  }

  public CtConstructor getCtConstructor() {
    return ctConstructor;
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
