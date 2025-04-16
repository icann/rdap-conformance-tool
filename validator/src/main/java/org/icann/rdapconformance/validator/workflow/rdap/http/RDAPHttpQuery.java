package org.icann.rdapconformance.validator.workflow.rdap.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.UnknownHostException;
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
import org.icann.rdapconformance.validator.workflow.rdap.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

public class RDAPHttpQuery implements RDAPQuery {

    private static final int HTTP_NOT_FOUND = 404;
    private static final int ZERO = 0;

    private static final String SEMI_COLON = ";";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String LOCATION = "Location";
    private static final String APPLICATION_RDAP_JSON = "application/rdap+JSON";
    public static final String OBJECT_CLASS_NAME = "objectClassName";
    public static final String ENTITIES = "entities";
    public static final String NAMESERVERS = "nameservers";
    public static final String AUTNUMS = "autnums";
    public static final String NETWORKS = "networks";
    public static final String ERROR_CODE = "errorCode";
    public static final String RDAP_CONFORMANCE = "rdapConformance";

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
          this.makeRequest(this.config.getUri());
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
              addErrorToResultsFile(results, -13003, httpResponse.body(), "The response does not have an objectClassName string.");
          } else if (queryType.equals(RDAPQueryType.NAMESERVERS) && !jsonIsSearchResponse()) {
            logger.error("No JSON array in answer");
            addErrorToResultsFile(results,-13003, httpResponse.body(),"The response does not have an objectClassName string.");
          }
        }
        return true; // this always returns true
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


    public void makeRequest(URI currentUri ) {
        try {
            logger.info("Making request to: {}", currentUri); // ensure we log each request
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

            // if we exit the loop without a redirect, we have a final response
            httpResponse = response;
        } catch (Exception e) {
            handleRequestException(e); // catch for all subclasses of these exceptions
        }
    }


    private void validate() {
        // If it wasn't successful, we don't need to validate
        if (!isQuerySuccessful()) {
            logger.info("Querying wasn't successful .. don't validate ");
            return;
        }

        // else continue on
        int httpStatusCode = httpResponse.statusCode();
        HttpHeaders headers = httpResponse.headers();
        String rdapResponse = httpResponse.body();

        if(config.useRdapProfileFeb2024()) {
            if(!validateIfContainsErrorCode(httpStatusCode, rdapResponse)) {
                addErrorToResultsFile(results,-12107, rdapResponse,"The errorCode value is required in an error response.");
            }
        }

        // If a response is available to the tool, and the header Content-Type is not
        // application/rdap+JSON, error code -13000 added in results file.
        if (Arrays.stream(String.join(SEMI_COLON, headers.allValues(CONTENT_TYPE)).split(SEMI_COLON))
                  .noneMatch(s -> s.equalsIgnoreCase(APPLICATION_RDAP_JSON))) {
            addErrorToResultsFile(results,-13000,
                                  headers.firstValue(CONTENT_TYPE).orElse("missing"), "The content-type header does not contain the application/rdap+json media type.");
        }

        // If a response is available to the tool, but it's not syntactically valid JSON object, error code -13001 added in results file.
        jsonResponse = new JsonData(rdapResponse);
        if (!jsonResponse.isValid()) {
            addErrorToResultsFile(results,-13001, "response body not given","The response was not valid JSON.");
            isQuerySuccessful = false;
          return;
        }

        // If a response is available to the tool, but the HTTP status code is not 200 nor 404, error code -13002 added in results file
        if (!List.of(HTTP_OK, HTTP_NOT_FOUND).contains(httpStatusCode)) {
            logger.error("Invalid HTTP status {}", httpStatusCode);
            addErrorToResultsFile(results,-13002, String.valueOf(httpStatusCode), "The HTTP status code was neither 200 nor 404.");
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
                addErrorToResultsFile(results,-13004, "<location header value>", "Response redirect contained query parameters copied from the request.");
                return true;
            }
        }
        return false; // not copying them, so don't worry
    }

    /**
     * Handle exceptions that occur during the HTTP request.
     */
  private void handleRequestException(Exception e) {

//    logger.info("Exception during RDAP query", e);
    if (e instanceof ConnectException || e instanceof HttpTimeoutException) {

      status = hasCause(e, "java.nio.channels.UnresolvedAddressException")
          ? RDAPValidationStatus.NETWORK_SEND_FAIL
          : RDAPValidationStatus.CONNECTION_FAILED;
      return;
    }

    if(e instanceof UnknownHostException) {
      status = RDAPValidationStatus.UNKNOWN_HOST_NAME;
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
    } else if(e.getMessage().contains("Read timed out")) {
        return RDAPValidationStatus.CONNECTION_FAILED;
    }

    return RDAPValidationStatus.NETWORK_RECEIVE_FAIL;
  }

  /**
   * Check if the RDAP json response contains a specific key.
   */
  public boolean jsonResponseValid() {
      if (jsonResponse == null) {
          return false;
      }

      boolean objectClassExists = true;
      if(!jsonResponse.hasKey("objectClassName")) {
          logger.info("Validating objectClass property in top level");
          objectClassExists = false;
      }

      Object entitiesObj = jsonResponse.getValue(ENTITIES);
      if(objectClassExists && entitiesObj instanceof List<?> entities) {
          logger.info("Validating objectClass property in entities");
          objectClassExists = verifyIfObjectClassPropExits(entities, ENTITIES);
          if(objectClassExists) {
              objectClassExists = verifyIfObjectClassPropExits(entities, AUTNUMS);
          }

          if(objectClassExists) {
              objectClassExists = verifyIfObjectClassPropExits(entities, NETWORKS);
          }
      }

      Object nameserversObj = jsonResponse.getValue(NAMESERVERS);
      if(objectClassExists && nameserversObj instanceof List<?> nameservers) {
          logger.info("Validating objectClass property in nameservers");
          objectClassExists = verifyIfObjectClassPropExits(nameservers, NAMESERVERS);
      }

    return null != jsonResponse && objectClassExists;
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
          logger.info("Invalid JSON in RDAP response");
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

    public boolean verifyIfObjectClassPropExits(List<?> propertyCollection, String containedKey) {
        for (var x: propertyCollection) {
            if (!(x instanceof Map<?, ?> map)) {
                return false;
            }
            var value = map.get(OBJECT_CLASS_NAME);
            if(value == null) {
                return false;
            }
            if (map.containsKey(containedKey)) {
                var nestedList = map.get(containedKey);
                if (nestedList instanceof List<?> nestedListCast) {
                    if (!verifyIfObjectClassPropExits(nestedListCast, containedKey)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean validateIfContainsErrorCode(int httpStatusCode, String rdapResponse) {
        if (httpStatusCode == HTTP_OK) {
            return true; // obviously we don't have to check then, pass
        }

        if (rdapResponse == null || rdapResponse.isBlank()) {
            logger.info("Empty response body for HTTP status code: {}", httpStatusCode);
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(rdapResponse, new TypeReference<Map<String, Object>>() {
            });
            return responseMap.containsKey(ERROR_CODE) && responseMap.containsKey(RDAP_CONFORMANCE);
        } catch (Exception e) {
            logger.info("Error parsing JSON response for error code check", e);
            return false;
        }
    }

    public static boolean isRedirectStatus(int status) {
        return switch (status) {
            case 301, 302, 303, 307, 308 -> true;
            default -> false;
        };
    }
}