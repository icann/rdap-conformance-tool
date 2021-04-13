package org.icann.rdapconformance.validator.workflow.rdap.http;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.Security;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPHttpQuery implements RDAPQuery {

  private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQuery.class);

  private final RDAPValidatorConfiguration config;
  private HttpResponse<String> httpResponse = null;
  private RDAPValidationStatus status = null;
  private JsonData jsonResponse = null;


  public RDAPHttpQuery(RDAPValidatorConfiguration config) {
    this.config = config;
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
     * not validation on the contents) for a search query, exit with an return code of 8.
     */
    if (httpResponse.statusCode() == 200) {
      if (queryType.isLookupQuery() && !jsonResponseValid()) {
        logger.error("objectClassName was not found in the topmost object");
        status = RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND;
        return false;
      } else if (queryType.equals(RDAPQueryType.NAMESERVERS) && !jsonResponseIsArray()) {
        logger.error("No JSON array in answer");
        status = RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND;
        return false;
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

  /**
   * Check if we got errors with the RDAP HTTP request.
   */
  private boolean isQuerySuccessful() {
    return status == null;
  }


  /**
   * Get the RDAP status in case of error
   */
  public RDAPValidationStatus getErrorStatus() {
    return status;
  }


  private void makeRequest() {
    final URI uri = this.config.getUri();
    /*
     * If the scheme of the URI is "https", the tool will initiate a TLS connection to the server:
     *  • The tool shall not try to match CA certs (if available) to a well-known CA.
     *  • If the CRL or OCSP is unavailable, this won't constitute an error, but if CRL or OCSP are
     *    accessible and indicate that the server certificate is revoked, the revocation
     *    constitutes an error.
     */
    if (uri.getScheme().equals("https")) {
      Security.setProperty("ocsp.enable", String.valueOf(true));
      System.setProperty("com.sun.net.ssl.checkRevocation", String.valueOf(true));
      System.setProperty("com.sun.security.enableCRLDP", String.valueOf(true));
      System.setProperty("com.sun.net.ssl.checkRevocation", String.valueOf(true));
    }

    System.setProperty("jdk.httpclient.redirects.retrylimit",
        String.valueOf(this.config.getMaxRedirects()));
    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .version(Version.HTTP_2)
        .timeout(Duration.of(this.config.getTimeout(), SECONDS))
        .GET()
        .build();
    try {
      httpResponse = java.net.http.HttpClient.newBuilder()
          .connectTimeout(Duration.of(this.config.getTimeout(), SECONDS))
          .followRedirects(Redirect.NORMAL)
          .build()
          .send(request, HttpResponse.BodyHandlers.ofString());
    } catch (ConnectException | HttpTimeoutException e) {
      status = RDAPValidationStatus.CONNECTION_FAILED;
      if (hasCause(e, "UnresolvedAddressException")) {
        status = RDAPValidationStatus.NETWORK_SEND_FAIL;
      }
    } catch (IOException e) {
      status = RDAPValidationStatus.NETWORK_RECEIVE_FAIL;
      if (hasCause(e, "CertificateExpiredException")) {
        status = RDAPValidationStatus.EXPIRED_CERTIFICATE;
      } else if (hasCause(e, "CertificateRevokedException")) {
        status = RDAPValidationStatus.REVOKED_CERTIFICATE;
      } else if (hasCause(e, "SSLHandshakeException") || hasCause(e, "CertificateException")) {
        if (e.getMessage().startsWith("No name matching") || e.getMessage()
            .startsWith("No subject alternative DNS name matching")) {
          status = RDAPValidationStatus.INVALID_CERTIFICATE;
        } else {
          status = RDAPValidationStatus.CERTIFICATE_ERROR;
        }
      }
    } catch (Exception e) {
      status = RDAPValidationStatus.CONNECTION_FAILED;
    }
  }

  private void validate() {
    if (!isQuerySuccessful()) {
      return;
    }

    int httpStatusCode = httpResponse.statusCode();
    if (String.valueOf(httpStatusCode).startsWith("30")) {
      status = RDAPValidationStatus.TOO_MANY_REDIRECTS;
      return;
    }

    /*
     * If a response is available to the tool, and the header Content-Type is not
     * application/rdap+JSON, exit with a return code of 5.
     */
    HttpHeaders headers = httpResponse.headers();
    if (Arrays.stream(String.join(";", headers.allValues("Content-Type")).split(";"))
        .noneMatch(s -> s.equalsIgnoreCase("application/rdap+JSON"))) {
      logger.error("Content-Type is {}, should be application/rdap+JSON",
          headers.firstValue("Content-Type").orElse("missing"));
      status = RDAPValidationStatus.WRONG_CONTENT_TYPE;
      return;
    }

    /*
     * If a response is available to the tool, but it's not syntactically valid JSON object, exit
     * with a return code of 6.
     */
    String rdapResponse = httpResponse.body();
    jsonResponse = new JsonData(rdapResponse);
    if (!jsonResponse.isValid()) {
      status = RDAPValidationStatus.RESPONSE_INVALID;
      return;
    }

    /* If a response is available to the tool, but the HTTP status code is not 200 nor 404, exit
     * with a return code of 7.
     */
    if (!List.of(200, 404).contains(httpStatusCode)) {
      logger.error("Invalid HTTP status {}", httpStatusCode);
      status = RDAPValidationStatus.INVALID_HTTP_STATUS;
      return;
    }
  }

  /**
   * Check if the RDAP json response contains a specific key.
   */
  private boolean jsonResponseValid() {
    return null != jsonResponse && jsonResponse.hasKey("objectClassName");
  }

  /**
   * Check if the RDAP json response is a JSON array
   */
  boolean jsonResponseIsArray() {
    return null != jsonResponse && jsonResponse.isArray();
  }

  private boolean hasCause(Throwable e, String causeClassName) {
    while (e.getCause() != null) {
      if (e.getCause().getClass().getSimpleName().equals(causeClassName)) {
        return true;
      }
      e = e.getCause();
    }
    return false;
  }

  static class JsonData {

    private JSONObject rawJsonObject = null;
    private JSONArray rawJsonArray = null;


    private JsonData(String data) {
      try {
        // FIXME JSONObject may be too permissive:
        //  - accepts , after last element
        //  - accepts string without " around
        rawJsonObject = new JSONObject(data);
      } catch (JSONException e1) {
        // JSON content may be a list
        try {
          rawJsonArray = new JSONArray(data);
        } catch (Exception e2) {
          logger.error("Invalid JSON in RDAP response");
        }
      }
    }

    /**
     * Parse the data into JSON.
     */
    public boolean isValid() {
      return rawJsonObject != null || rawJsonArray != null;
    }

    /**
     * Check whether the JSON data is an array.
     */
    public boolean isArray() {
      return rawJsonArray != null;
    }

    public boolean hasKey(String key) {
      return isValid() && !isArray() && null != rawJsonObject.opt(key);
    }
  }
}
