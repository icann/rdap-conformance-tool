package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.Objects;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.NetworkInfo;

public class RDAPValidationResult {

  private final int code;
  private final String value;
  private final String message;
  private final String acceptHeader;
  private final String httpMethod;
  private final String serverIpAddress;
  private final String httpStatusCode;

  public RDAPValidationResult(int code, String value, String message, String acceptHeader, String httpMethod, String serverIpAddress, String httpStatusCode) {
    this.code = code;
    this.value = value;
    this.message = message;
    this.acceptHeader = acceptHeader;
    this.httpMethod = httpMethod;
    this.serverIpAddress = serverIpAddress;
    this.httpStatusCode = httpStatusCode;
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getCode() {
    return code;
  }

  public String getValue() {
    return value;
  }

  public String getMessage() {
    return message;
  }

  public String getAcceptHeader() {
    return acceptHeader;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getServerIpAddress() {
    return serverIpAddress;
  }

  public String getHttpStatusCode() { return httpStatusCode; }

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
        Objects.equals(value, result.value) &&
        Objects.equals(message, result.message) &&
        Objects.equals(acceptHeader, result.acceptHeader) &&
        Objects.equals(httpMethod, result.httpMethod) &&
        Objects.equals(serverIpAddress, result.serverIpAddress) &&
        Objects.equals(httpStatusCode, result.httpStatusCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, value, message, acceptHeader, httpMethod, serverIpAddress, httpStatusCode);
  }

  @Override
  public String toString() {
    return "RDAPValidationResult{" +
        "code=" + code +
        ", value='" + value + '\'' +
        ", message='" + message + '\'' +
        ", acceptHeader='" + acceptHeader + '\'' +
        ", httpMethod='" + httpMethod + '\'' +
        ", serverIpAddress='" + serverIpAddress + '\'' +
        ", httpStatusCode='" + httpStatusCode + '\'' +
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
      return new RDAPValidationResult(
          this.code,
          this.value,
          this.message,
          // these are a snapshot of the current state of the connection
          NetworkInfo.getAcceptHeader(),
          NetworkInfo.getHttpMethod(),
          NetworkInfo.getServerIpAddress(),
          ConnectionTracker.getCurrentStatusCodeAsString()
      );
    }
  }
}