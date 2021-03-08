package org.icann.rdap.conformance.validator;

public class RDAPValidationResult {

  private String code;
  private String value;
  private String message;

  public RDAPValidationResult(String code, String value, String message) {
    this.code = code;
    this.value = value;
    this.message = message;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  static public Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String code;
    private String value;
    private String message;

    public Builder code(String code) {
      this.code = code;
      return this;
    }

    public Builder value(String value) {
      this.value = value;
      return this;
    }

    public Builder message(String message) {
      this.message = message;
      return this;
    }

    public RDAPValidationResult build() {
      return new RDAPValidationResult(this.code, this.value, this.message);
    }
  }
}
