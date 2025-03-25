package org.icann.rdapconformance.validator.workflow.rdap.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPHttpQuery implements RDAPQuery {

  private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQuery.class);

  private final RDAPValidatorConfiguration config;
  private RDAPValidatorResults results = null;
  private HttpResponse<String> httpResponse = null;
  private RDAPValidationStatus status = null;
  private JsonData jsonResponse = null;
  private boolean isQuerySuccessful = true;


  public RDAPHttpQuery(RDAPValidatorConfiguration config) {
    this.config = config;
    /*
     * If the scheme of the URI is "https", the tool will initiate a TLS connection to the server:
     *  • The tool shall not try to match CA certs (if available) to a well-known CA.
     *  • If the CRL or OCSP is unavailable, this won't constitute an error, but if CRL or OCSP are
     *    accessible and indicate that the server certificate is revoked, the revocation
     *    constitutes an error.
     */
    Security.setProperty("ocsp.enable", String.valueOf(true));
    System.setProperty("com.sun.net.ssl.checkRevocation", String.valueOf(true));
    System.setProperty("com.sun.security.enableCRLDP", String.valueOf(true));
    System.setProperty("com.sun.net.ssl.checkRevocation", String.valueOf(true));
    System.setProperty("jdk.httpclient.redirects.retrylimit",
        String.valueOf(this.config.getMaxRedirects()));
  }

  /**
   * Launch the HTTP request and validate it.
   */
  @Override
  public boolean run() {
    this.makeRequest();
    this.validate();
    return this.isQuerySuccessful();
  }

  /**
   * Get the HTTP response status code
   */
  @Override
  public Optional<Integer> getStatusCode() {
    return Optional.ofNullable(httpResponse != null ? httpResponse.statusCode() : null);
  }

  @Override
  public boolean checkWithQueryType(RDAPQueryType queryType) {
    /*
     * If a response is available to the tool, but the expected objectClassName in the topmost
     * object was not found for a lookup query (i.e. domain/<domain name>,
     * nameserver/<nameserver name> and entity/<handle>) nor the expected JSON array
     * (i.e. nameservers?ip=<nameserver search pattern>, just the JSON array should exist,
     * not validation on the contents) for a search query, code error -13003 added in results file.
     */
    if (httpResponse.statusCode() == 200) {
      if (queryType.isLookupQuery() && !jsonResponseValid()) {
        logger.error("objectClassName was not found in the topmost object");
        results.add(RDAPValidationResult.builder()
                .code(-13003)
                .value(httpResponse.body())
                .message("The response does not have an objectClassName string.")
                .build());
      } else if (queryType.equals(RDAPQueryType.NAMESERVERS) && !jsonIsSearchResponse()) {
        logger.error("No JSON array in answer");
        results.add(RDAPValidationResult.builder()
                .code(-13003)
                .value(httpResponse.body())
                .message("The response does not have an objectClassName string.")
                .build());
      }
    }
    return true;
  }

  @Override
  public boolean isErrorContent() {
    return httpResponse.statusCode() == 404;
  }

  @Override
  public String getData() {
    return httpResponse.body();
  }

  @Override
  public Object getRawResponse() {
    return httpResponse;
  }

  @Override
  public void setResults(RDAPValidatorResults results) {
    this.results = results;
  }

  /**
   * Check if we got errors with the RDAP HTTP request.
   */
  private boolean isQuerySuccessful() {
    return status == null && isQuerySuccessful;
  }


  /**
   * Get the RDAP status in case of error
   */
  public RDAPValidationStatus getErrorStatus() {
    return status;
  }


  private void makeRequest() {
    try {
      httpResponse = RDAPHttpRequest.makeHttpGetRequest(this.config.getUri(), this.config.getTimeout());
    } catch (ConnectException | HttpTimeoutException e) {
      logger.info("Exception when connecting to RDAP server", e);
      status = RDAPValidationStatus.CONNECTION_FAILED;
      if (hasCause(e, "java.nio.channels.UnresolvedAddressException")) {
        status = RDAPValidationStatus.NETWORK_SEND_FAIL;
      }
    } catch (IOException e) {
      logger.info("Exception receiving data from the RDAP server", e);
      status = RDAPValidationStatus.NETWORK_RECEIVE_FAIL;
      if (hasCause(e, "java.security.cert.CertificateExpiredException")) {
        status = RDAPValidationStatus.EXPIRED_CERTIFICATE;
      } else if (hasCause(e, "java.security.cert.CertificateRevokedException")) {
        status = RDAPValidationStatus.REVOKED_CERTIFICATE;
      } else if (hasCause(e, "java.security.cert.CertificateException")) {
        status = RDAPValidationStatus.CERTIFICATE_ERROR;
        if (e.getMessage().startsWith("No name matching") || e.getMessage()
            .startsWith("No subject alternative DNS name matching")) {
          status = RDAPValidationStatus.INVALID_CERTIFICATE;
        }
      } else if (hasCause(e, "javax.net.ssl.SSLHandshakeException")) {
        status = RDAPValidationStatus.HANDSHAKE_FAILED;
      } else if (hasCause(e, "sun.security.validator.ValidatorException")) {
        status = RDAPValidationStatus.CERTIFICATE_ERROR;
      }
    } catch (Exception e) {
      logger.info("Exception with RDAP query", e);
      status = RDAPValidationStatus.CONNECTION_FAILED;
    }
  }

  private void validate() {
    if (!isQuerySuccessful()) {
      return;
    }

    int httpStatusCode = httpResponse.statusCode();
    if (String.valueOf(httpStatusCode).startsWith("30")) {
      logger.error("Received to many redirects");
      status = RDAPValidationStatus.TOO_MANY_REDIRECTS;
      return;
    }

    /*
     * If a response is available to the tool, and the header Content-Type is not
     * application/rdap+JSON, error code -13000 added in results file.
     */
    HttpHeaders headers = httpResponse.headers();
    if (Arrays.stream(String.join(";", headers.allValues("Content-Type")).split(";"))
        .noneMatch(s -> s.equalsIgnoreCase("application/rdap+JSON"))) {
      results.add(RDAPValidationResult.builder()
              .code(-13000)
              .value(headers.firstValue("Content-Type").orElse("missing"))
              .message("The content-type header does not contain the application/rdap+json media type.")
              .build());
    }

    /*
     * If a response is available to the tool, but it's not syntactically valid JSON object, error
     * code -13001 added in results file.
     */
    String rdapResponse = httpResponse.body();
    jsonResponse = new JsonData(rdapResponse);
    if (!jsonResponse.isValid()) {
      results.add(RDAPValidationResult.builder()
                      .code(-13001)
                      .value("response body not given")
                      .message("The response was not valid JSON.")
                      .build());

      isQuerySuccessful = false;
      return;
    }

    /* If a response is available to the tool, but the HTTP status code is not 200 nor 404, error
     * code -13002 added in results file
     */
    if (!List.of(200, 404).contains(httpStatusCode)) {
      logger.error("Invalid HTTP status {}", httpStatusCode);
      results.add(RDAPValidationResult.builder()
          .code(-13002)
          .value(String.valueOf(httpStatusCode))
          .message("The HTTP status code was neither 200 nor 404.")
          .build());

      isQuerySuccessful = false;
    }
  }

  /**
   * Check if the RDAP json response contains a specific key.
   */

  private boolean jsonResponseValid() {
    return null != jsonResponse && jsonResponse.hasKey("objectClassName");
  }

  /**
   * Check if the RDAP is a JSON array results response
   */
  boolean jsonIsSearchResponse() {
    return null != jsonResponse && jsonResponse.hasKey("nameserverSearchResults")
        && jsonResponse.getValue("nameserverSearchResults") instanceof Collection<?>;
  }

  private boolean hasCause(Throwable e, String causeClassName) {
    while (e.getCause() != null) {
      if (e.getCause().getClass().getName().equals(causeClassName)) {
        return true;
      }
      e = e.getCause();
    }
    return false;
  }

  static class JsonData {

    private Map<String, Object> rawRdapMap = null;
    private List<Object> rawRdapList = null;


    private JsonData(String data) {
      ObjectMapper mapper = new ObjectMapper();

      try {
        rawRdapMap = mapper.readValue(data, Map.class);
      } catch (Exception e1) {
        // JSON content may be a list
        try {
          rawRdapList = mapper.readValue(data, List.class);
        } catch (Exception e2) {
          logger.error("Invalid JSON in RDAP response");
        }
      }
    }

    /**
     * Parse the data into JSON.
     */
    public boolean isValid() {
      return rawRdapMap != null || rawRdapList != null;
    }

    /**
     * Check whether the JSON data is an array.
     */
    public boolean isArray() {
      return rawRdapList != null;
    }

    public boolean hasKey(String key) {
      return isValid() && !isArray() && rawRdapMap.containsKey(key);
    }

    public Object getValue(String key) {
      return rawRdapMap.get(key);
    }
  }
}
