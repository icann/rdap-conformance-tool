package org.icann.rdapconformance.validator.schema;

public class NullValidationNode extends ValidationNode {

  public NullValidationNode() {
    super();
  }

  @Override
  public boolean hasParentValidationCode() {
    return false;
  }
}
