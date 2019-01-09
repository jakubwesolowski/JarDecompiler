package jar;

import javassist.CtClass;
import javassist.CtField;

public class JarField {

  private CtClass ctClass;
  private CtField ctField;

  public JarField(CtClass ctClass, CtField field) {
    this.ctClass = ctClass;
    this.ctField = field;
  }

  public CtField getCtField() {
    return ctField;
  }

  public CtClass getCtClass() {
    return ctClass;
  }

  @Override
  public String toString() {
    return ctField.getName();
  }
}
