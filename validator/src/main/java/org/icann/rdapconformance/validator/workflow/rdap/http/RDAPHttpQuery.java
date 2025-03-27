package org.icann.rdapconformance.validator.workflow.rdap.http;

import static java.net.HttpURLConnection.HTTP_OK;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidationTestInvalidDomain;
import org.icann.rdapconformance.validator.workflow.rdap.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPHttpQuery implements RDAPQuery {

    private static final int HTTP_NOT_FOUND = 404;
    private static final int ZERO = 0;

    private static final String SEMI_COLON = ";";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String LOCATION = "Location";
    private static final String NAMESERVER_SEARCH_RESULTS = "nameserverSearchResults";
    private static final String APPLICATION_RDAP_JSON = "application/rdap+JSON";

    private List<URI> redirects = new ArrayList<>();
    private String acceptHeader;
    private final RDAPValidatorConfiguration config;
    private RDAPValidatorResults results = null;
    private HttpResponse<String> httpResponse = null;
    private RDAPValidationStatus status = null;
    private JsonData jsonResponse = null;
    private boolean isQuerySuccessful = true;

    private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQuery.class);

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

  // These two (getRedirects and getAcceptHeader) are specific to HTTP queries
  /**
   * Get the list of redirects
   */
  public List<URI> getRedirects() {
    return redirects;
  }

  /**
   * Get the Accept header
   */
  public String getAcceptHeader() {
    return acceptHeader;
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
    if (httpResponse.statusCode() == HTTP_OK) {
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
        return httpResponse.statusCode() == HTTP_NOT_FOUND;
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

    public void makeRequest() {
        try {
            URI currentUri = this.config.getUri();
            int remainingRedirects = this.config.getMaxRedirects();
            HttpResponse<String> response = null;

            while (remainingRedirects > ZERO) {
                response = RDAPHttpRequest.makeHttpGetRequest(currentUri, this.config.getTimeout());
                int status = response.statusCode();

                if (isRedirectStatus(status)) {
                    Optional<String> location = response.headers().firstValue(LOCATION);
                    if (location.isEmpty()) {
                        break; // can't follow if no location header
                    }

                    URI redirectUri = URI.create(location.get());
                    if(ResponseValidationTestInvalidDomain.isRedirectingTestDotInvalidToItself(results, currentUri, redirectUri)) {
                        logger.info("Server responded with a redirect to itself for domain '{}'.", currentUri);
                        return;
                    }

                    if (!redirectUri.isAbsolute()) {
                        redirectUri = currentUri.resolve(redirectUri);
                    }

                    redirects.add(redirectUri);
                    logger.info("Redirecting to: {}", redirectUri);

                    // this check is only done on redirects
                    if (isBlindlyCopyingParams(response.headers())) {
                        httpResponse = response; // Set the response before returning
                        return;
                    }

                    currentUri = redirectUri;
                    remainingRedirects--;
                } else {
                    break; // Not a redirect
                }
            }

            // check for the redirects
            if (remainingRedirects == ZERO) {
                status = RDAPValidationStatus.TOO_MANY_REDIRECTS;
            }

            httpResponse = response;
        } catch (Exception e) {
            handleRequestException(e); // catch for all subclasses of these exceptions
        }
    }

    private boolean isRedirectStatus(int status) {
        return switch (status) {
            case 301, 302, 303, 307, 308 -> true;
            default -> false;
        };
    }

    private void validate() {
        // If it wasn't successful, we don't need to validate
        if (!isQuerySuccessful()) {
            return;
        }

        // else continue on
        int httpStatusCode = httpResponse.statusCode();
        HttpHeaders headers = httpResponse.headers();
        String rdapResponse = httpResponse.body();

        // If a response is available to the tool, and the header Content-Type is not
        //  application/rdap+JSON, error code -13000 added in results file.
        if (Arrays.stream(String.join(SEMI_COLON, headers.allValues(CONTENT_TYPE)).split(SEMI_COLON))
                  .noneMatch(s -> s.equalsIgnoreCase(APPLICATION_RDAP_JSON))) {
            results.add(RDAPValidationResult.builder()
                                            .code(-13000)
                                            .value(headers.firstValue(CONTENT_TYPE).orElse("missing"))
                                            .message(
                                                "The content-type header does not contain the application/rdap+json media type.")
                                            .build());
        }

        // If a response is available to the tool, but it's not syntactically valid JSON object, error code -13001 added in results file.
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

        // If a response is available to the tool, but the HTTP status code is not 200 nor 404, error code -13002 added in results file
        if (!List.of(HTTP_OK, HTTP_NOT_FOUND).contains(httpStatusCode)) {
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
     * Check if the query parameters are copied into the Location header.
     */
    public boolean isBlindlyCopyingParams(HttpHeaders headers) {
        // Check if query parameters are copied into the Location header
        Optional<String> locationHeader = headers.firstValue(LOCATION);
        if (locationHeader.isPresent()) {
            URI originalUri = config.getUri();
            URI locationUri = URI.create(locationHeader.get());

            String originalQuery = originalUri.getQuery();
            String locationQuery = locationUri.getQuery();

            // They copied the query over, this is bad
            if (originalQuery != null && originalQuery.equals(locationQuery)) {
                results.add(RDAPValidationResult.builder()
                                                .code(-13004)
                                                .value("<location header value>")
                                                .message(
                                                    "Response redirect contained query parameters copied from the request.")
                                                .build());
                return true;
            }
        }
        return false; // not copying them, so don't worry
    }

    /**
     * Handle exceptions that occur during the HTTP request.
     */
  private void handleRequestException(Exception e) {
    logger.info("Exception during RDAP query", e);
    if (e instanceof ConnectException || e instanceof HttpTimeoutException) {
      status = hasCause(e, "java.nio.channels.UnresolvedAddressException")
          ? RDAPValidationStatus.NETWORK_SEND_FAIL
          : RDAPValidationStatus.CONNECTION_FAILED;
      return;
    }

    if (e instanceof IOException) {
      status = analyzeIOException((IOException) e);
      return;
    }

    status = RDAPValidationStatus.CONNECTION_FAILED;
  }

  /* *
   * Analyze the IOException to determine the RDAP validation status.
   */
  private RDAPValidationStatus analyzeIOException(IOException e) {
    if (hasCause(e, "java.security.cert.CertificateExpiredException")) {
      return RDAPValidationStatus.EXPIRED_CERTIFICATE;
    } else if (hasCause(e, "java.security.cert.CertificateRevokedException")) {
      return RDAPValidationStatus.REVOKED_CERTIFICATE;
    } else if (hasCause(e, "java.security.cert.CertificateException")) {
        if (e.getMessage().contains("No name matching") ||
            e.getMessage().contains("No subject alternative DNS name matching")) {
            return RDAPValidationStatus.INVALID_CERTIFICATE;
        }
      return RDAPValidationStatus.CERTIFICATE_ERROR;
    } else if (hasCause(e, "javax.net.ssl.SSLHandshakeException")) {
      return RDAPValidationStatus.HANDSHAKE_FAILED;
    } else if (hasCause(e, "sun.security.validator.ValidatorException")) {
      return RDAPValidationStatus.CERTIFICATE_ERROR;
    }

    return RDAPValidationStatus.NETWORK_RECEIVE_FAIL;
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