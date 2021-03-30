package org.icann.rdapconformance.validator;

import java.util.Objects;

public class RDAPValidationResult {

  private int code;
  private String value;
  private String message;

  public RDAPValidationResult(int code, String value, String message) {
    this.code = code;
    this.value = value;
    this.message = message;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RDAPValidationResult result = (RDAPValidationResult) o;
    return code == result.code &&
        value.equals(result.value) &&
        message.equals(result.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, value, message);
  }

  static public Builder builder() {
    return new Builder();
  }

  @Override
  public String toString() {
    return "RDAPValidationResult{" +
        "code=" + code +
        ", message='" + message + '\'' +
        ", value='" + value + '\'' +
        '}';
  }

  public static class Builder {

    private int code;
    private String value;
    private String message;

    public Builder code(int code) {
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
