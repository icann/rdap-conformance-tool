package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.CommonUtils.GET;
import java.util.Objects;

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

    private int code;
    private String value;
    private String message;
    private String acceptHeader;
    private String httpMethod;
    private Integer httpStatusCode;
    private String queriedURI;
    private String serverIpAddress;

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


    /**
     * Build RDAPValidationResult using QueryContext for proper status code and network info.
     * This method should be preferred over build() when QueryContext is available.
     */
    public RDAPValidationResult build(org.icann.rdapconformance.validator.QueryContext queryContext) {
      Integer statusCodeFromCurrent = null;

      // First try to get status code from current HTTP response in QueryContext
      if (queryContext != null && queryContext.getCurrentHttpResponse() != null) {
        statusCodeFromCurrent = queryContext.getCurrentHttpResponse().statusCode();
      }

      // Fallback to ConnectionTracker if HTTP response not available
      if (statusCodeFromCurrent == null && queryContext != null && queryContext.getConnectionTracker() != null) {
        org.icann.rdapconformance.validator.ConnectionTracker.ConnectionRecord mainConnection =
            queryContext.getConnectionTracker().getLastMainConnection();
        if (mainConnection != null && mainConnection.getStatusCode() != 0 && mainConnection.getStatus() != null) {
          statusCodeFromCurrent = mainConnection.getStatusCode();
        } else {
          statusCodeFromCurrent = 0; // force to zero
        }
      }

      return new RDAPValidationResult(
          this.code,
          this.value,
          this.message,
          this.acceptHeader != null ? this.acceptHeader :
              (queryContext != null ? queryContext.getNetworkInfo().getAcceptHeaderValue() : "application/json"),
          this.httpMethod != null ? this.httpMethod : GET,
          this.serverIpAddress != null ? this.serverIpAddress :
              (queryContext != null ? queryContext.getNetworkInfo().getServerIpAddressValue() : "-"),
          this.httpStatusCode != null ? this.httpStatusCode : statusCodeFromCurrent,
          this.queriedURI != null ?
                  this.queriedURI : (queryContext != null && queryContext.getConfig() != null && queryContext.getConfig().getUri() != null
                  ? queryContext.getConfig().getUri().toString()
                  : "-")
      );
    }

    /**
     * Build method for testing purposes and fallback compatibility.
     * Used by tests and as fallback when QueryContext is not available.
     * @deprecated Use build(QueryContext) instead for proper HTTP status code handling in production.
     */
    @Deprecated
    public RDAPValidationResult build() {
        // Preserve old behavior for test compatibility: null HTTP values
        return new RDAPValidationResult(
            this.code,
            this.value,
            this.message,
            this.acceptHeader,
            this.httpMethod,
            this.serverIpAddress,
            this.httpStatusCode,
            this.queriedURI
        );
    }
  }
}