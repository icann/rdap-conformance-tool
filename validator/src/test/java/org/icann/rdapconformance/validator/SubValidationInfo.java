package org.icann.rdapconformance.validator;

class SubValidationInfo {

  public SubValidationInfo(String validationName, String validatedField, int errorCode) {
    this.validationName = validationName;
    this.validatedField = validatedField;
    this.errorCode = errorCode;
  }

  public String validationName;
  public String validatedField;
  public int errorCode;
}
