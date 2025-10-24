package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.CommonUtils.GET;

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
  private final Integer httpStatusCode;
  private final String queriedURI;

  public RDAPValidationResult(int code, String value, String message, String acceptHeader,
                              String httpMethod, String serverIpAddress,
                              Integer httpStatusCode, String queriedURI) {
    this.code = code;
    this.value = value;
    this.message = message;
    this.acceptHeader = acceptHeader;
    this.httpMethod = httpMethod;
    this.serverIpAddress = serverIpAddress;
    this.httpStatusCode = httpStatusCode;
    this.queriedURI = queriedURI;
  }

  public static Builder builder() {
    // For backward compatibility, use default session - but this should be avoided
    return new Builder(org.icann.rdapconformance.validator.session.SessionContext.DEFAULT_SESSION_ID);
  }

  public static Builder builder(String sessionId) {
    return new Builder(sessionId);
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

  public Integer getHttpStatusCode() { return httpStatusCode; }

  public String getQueriedURI() { return queriedURI; }

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
        Objects.equals(httpStatusCode, result.httpStatusCode) &&
        Objects.equals(queriedURI, result.queriedURI);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, value, message, acceptHeader, httpMethod, serverIpAddress, httpStatusCode, queriedURI);
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
        ", queriedURI='" + queriedURI + '\'' +
        '}';
  }

  public static class Builder {

    private final String sessionId;
    private int code;
    private String value;
    private String message;
    private String acceptHeader;
    private String httpMethod;
    private Integer httpStatusCode;
    private String queriedURI;
    private String serverIpAddress;

    public Builder(String sessionId) {
      this.sessionId = sessionId;
    }

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

    public Builder acceptHeader(String acceptHeader) {
      this.acceptHeader = acceptHeader;
      return this;
    }

    public Builder httpMethod(String httpMethod) {
      this.httpMethod = httpMethod;
      return this;
    }

    public Builder httpStatusCode(Integer httpStatusCode) {
      this.httpStatusCode = httpStatusCode;
      return this;
    }

    public Builder queriedURI(String queriedURI) {
      this.queriedURI = queriedURI;
      return this;
    }

    public Builder serverIpAddress(String serverIpAddress) {
      this.serverIpAddress = serverIpAddress;
      return this;
    }

    public RDAPValidationResult build() {
      Integer statusCodeFromCurrent = ConnectionTracker.getMainStatusCode(this.sessionId);

      return new RDAPValidationResult(
          this.code,
          this.value,
          this.message,
          this.acceptHeader != null ? this.acceptHeader : NetworkInfo.getAcceptHeader(),  // the default is the current accept header
          this.httpMethod != null ? this.httpMethod : GET, // the default is GET unless you explicitly set it
          this.serverIpAddress != null ? this.serverIpAddress :  NetworkInfo.getServerIpAddress(), // the default is the current server IP address
          this.httpStatusCode != null ? this.httpStatusCode : statusCodeFromCurrent,
          this.queriedURI
      );
    }
  }
}