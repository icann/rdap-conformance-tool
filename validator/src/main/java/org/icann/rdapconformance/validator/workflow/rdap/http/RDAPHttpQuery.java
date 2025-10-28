package org.icann.rdapconformance.validator.workflow.rdap.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

import java.util.*;
import org.icann.rdapconformance.validator.ConformanceError;
import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.*;

import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest.SimpleHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.icann.rdapconformance.validator.CommonUtils.CONTENT_TYPE;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP_NOT_FOUND;
import static org.icann.rdapconformance.validator.CommonUtils.LOCATION;
import static org.icann.rdapconformance.validator.CommonUtils.SEMI_COLON;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

/**
 * HTTP-based implementation of RDAP query operations with comprehensive validation.
 *
 * <p>This class handles all aspects of RDAP HTTP communication including request execution,
 * redirect handling, response validation, and JSON parsing. It implements the RDAPQuery
 * interface to provide a standardized way to perform RDAP operations over HTTP/HTTPS.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>HTTP request execution with configurable timeouts and redirect limits</li>
 *   <li>Automatic redirect following with Location header validation</li>
 *   <li>Content-Type validation for RDAP-specific media types</li>
 *   <li>JSON response parsing and structural validation</li>
 *   <li>Query type-specific validation (lookup vs search operations)</li>
 *   <li>Error handling and reporting integration with validation results</li>
 * </ul>
 *
 * <p>The class supports both RDAP lookup queries (single object retrieval) and search
 * queries (collection-based results), with appropriate validation for each type. It
 * tracks connection status, handles HTTP redirects, and validates response structure
 * according to RDAP specifications.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * RDAPHttpQuery query = new RDAPHttpQuery(configuration);
 * boolean success = query.run();
 * if (success) {
 *     String responseData = query.getData();
 *     // Process RDAP response data
 * } else {
 *     ConformanceError error = query.getErrorStatus();
 *     // Handle query failure
 * }
 * </pre>
 *
 * @see RDAPQuery
 * @see RDAPValidatorConfiguration
 * @see ConnectionTracker
 * @since 1.0.0
 */
public class RDAPHttpQuery implements RDAPQuery {

    private static final String APPLICATION_RDAP_JSON = "application/rdap+JSON";
    public static final String OBJECT_CLASS_NAME = "objectClassName";
    public static final String ENTITIES = "entities";
    public static final String NAMESERVERS = "nameservers";
    public static final String AUTNUMS = "autnums";
    public static final String NETWORKS = "networks";
    public static final String ERROR_CODE = "errorCode";
    public static final String RDAP_CONFORMANCE = "rdapConformance";
    public static final String NAMESERVER_SEARCH_RESULTS = "nameserverSearchResults";

    private List<URI> redirects = new ArrayList<>();
    private final RDAPValidatorConfiguration config;
    private HttpResponse<String> httpResponse = null;

    private JsonData jsonResponse = null;
    private boolean isQuerySuccessful = true;
    private ConnectionStatus status = null;

    // QueryContext for thread-safe network operations
    private org.icann.rdapconformance.validator.QueryContext queryContext;

    private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQuery.class);

    /**
     * Creates a new RDAP HTTP query with the specified configuration.
     *
     * @param config the validator configuration containing URI, timeout, and other settings
     */
    public RDAPHttpQuery(RDAPValidatorConfiguration config) {
        this.config = config;
    }

    /**
     * Sets the QueryContext for this query. This is called after construction
     * to provide access to thread-safe network operations.
     *
     * @param queryContext the QueryContext to use for network operations
     */
    public void setQueryContext(org.icann.rdapconformance.validator.QueryContext queryContext) {
        this.queryContext = queryContext;
    }

    /**
     * Executes the RDAP HTTP query and performs validation.
     *
     * <p>This method resets the query state, makes the HTTP request to the configured URI,
     * validates the response according to RDAP specifications, and returns the overall
     * success status. All validation errors are automatically added to the results file.</p>
     *
     * @return true if the query and validation completed successfully, false otherwise
     */
    @Override
    public boolean run() {
        //TODO ok, this needs to be abstracted into a state holding instance
        // without this we error out forever after a single failure
        this.isQuerySuccessful = true; // reset to the default state
        this.status = null; // same
        // continue on
        this.makeRequest(this.config.getUri());
        this.validate();
        return this.isQuerySuccessful();
    }

    /**
     * Returns the list of redirect URIs encountered during the HTTP request.
     *
     * <p>This list contains all the redirect locations that were followed during
     * the request execution, in the order they were encountered. This is useful
     * for debugging and understanding the redirect chain.</p>
     *
     * @return list of URIs that the request was redirected to, may be empty
     */
    public List<URI> getRedirects() {
        return redirects;
    }


    /**
     * Validates if the RDAP response matches the expected format for the given query type.
     *
     * For lookup queries: checks if the response contains a valid objectClassName. For nameserver search queries:
     * checks if the response contains a nameserverSearchResults collection.
     *
     * Adds appropriate error codes to results file if validation fails: - Error -13003: Missing objectClassName in
     * lookup query response - Error -12610: Missing nameserverSearchResults in nameserver search query (RDAP Profile
     * Feb 2024)
     *
     * @param queryType The type of RDAP query performed
     */
    public boolean validateStructureByQueryType(RDAPQueryType queryType) {
        // default to true. Note: we do NOT return early and a false doesn't mean anything other than the structure contains some errors
        boolean structureValid = true;
        if (httpResponse.statusCode() == HTTP_OK) {
            if (queryType.isLookupQuery() && !jsonResponseValid()) {
                logger.debug("objectClassName was not found in the topmost object");
                queryContext.addError(-13003, httpResponse.body(),
                    "The response does not have an objectClassName string.");
                structureValid = false;
            } else if (queryType.equals(RDAPQueryType.NAMESERVERS) && !hasNameserverSearchResults()) {
                logger.debug("No JSON array in answer");
                if (config.useRdapProfileFeb2024()) {
                    queryContext.addError(-12610, httpResponse.body(),
                        "The nameserverSearchResults structure is required.");
                    structureValid = false;
                } else {
                    queryContext.addError(-13003, httpResponse.body(),
                        "The response does not have an objectClassName string.");
                    structureValid = false;
                }
            }
        }
        return structureValid;
    }

    /**
     * Determines if the HTTP response represents error content.
     *
     * <p>This method checks if the HTTP status code indicates an error response.
     * Currently, it considers 404 (Not Found) as the primary error indicator.</p>
     *
     * @return true if the response status code indicates error content, false otherwise
     */
    @Override
    public boolean isErrorContent() {
        return httpResponse.statusCode() == HTTP_NOT_FOUND;
    }

    /**
     * Returns the raw response body data from the HTTP request.
     *
     * @return the HTTP response body as a string, or null if no response was received
     */
    @Override
    public String getData() {
        return httpResponse.body();
    }

    /**
     * Returns the raw HTTP response object.
     *
     * <p>This provides access to the complete HTTP response including headers,
     * status code, and body for advanced processing or debugging purposes.</p>
     *
     * @return the HttpResponse object, or null if no response was received
     */
    @Override
    public Object getRawResponse() {
        return httpResponse;
    }


    /**
     * Checks if the RDAP HTTP query completed successfully.
     *
     * <p>A query is considered successful if there are no connection errors
     * (status is null or has code 0) and no validation failures occurred.</p>
     *
     * @return true if the query was successful, false otherwise
     */
    private boolean isQuerySuccessful() {
        return (status == null || status.getCode() == 0) && isQuerySuccessful;
    }

    /**
     * Returns the error status if the query failed.
     *
     * <p>This provides detailed information about connection or validation
     * failures that occurred during query execution.</p>
     *
     * @return the ConformanceError containing error details, or null if no error occurred
     */
    @Override
    public ConformanceError getErrorStatus() {
        return status;
    }

    /**
     * Sets the error status for this query.
     *
     * <p>This method is used to record connection or validation errors that
     * occur during query execution. A status code of 0 indicates success.</p>
     *
     * @param status the ConformanceError to set, expected to be a ConnectionStatus instance
     */
    @Override
    public void setErrorStatus(ConformanceError status) {
        this.status = (ConnectionStatus) status; // remember, 0 is ok - SUCCESS
    }

    /**
     * Executes the HTTP request to the specified URI with redirect handling.
     *
     * <p>This method handles the complete HTTP request lifecycle including automatic
     * redirect following (up to the configured limit), connection status tracking,
     * and validation of redirect parameters to prevent security issues.</p>
     *
     * @param currentUri the URI to make the initial request to
     */
    public void makeRequest(URI currentUri) {
        try {
            logger.debug("Making request to: {}", currentUri); // ensure we log each request
            int remainingRedirects = this.config.getMaxRedirects();
            HttpResponse<String> response = null;

            while (remainingRedirects > ZERO) {
                // QueryContext is required for proper network protocol handling
                if (queryContext == null) {
                    throw new IllegalStateException("QueryContext must be set before running RDAPHttpQuery. Call setQueryContext() first.");
                }
                logger.debug("Using QueryContext makeRequest - protocol: {}", queryContext.getNetworkProtocol());
                response = RDAPHttpRequest.makeRequest(queryContext, currentUri, this.config.getTimeout(), GET, true);
                int httpStatusCode = response.statusCode();
                ConnectionStatus st = ((SimpleHttpResponse) response).getConnectionStatusCode();
                this.setErrorStatus(((SimpleHttpResponse) response).getConnectionStatusCode());   // ensure this is set
                // TODO: we need to think about why we have this check in here and remove when we refactor the State Error/Success Handling
                if (httpStatusCode == ZERO) { // if our fake status code is 0, we have a problem
                    isQuerySuccessful = false;
                    return;
                }

                if (isRedirectStatus(httpStatusCode)) {
                    Optional<String> location = response.headers().firstValue(LOCATION);
                    if (location.isEmpty()) {
                        break; // can't follow if no location header
                    }

                    URI redirectUri = URI.create(location.get());

                    if (!redirectUri.isAbsolute()) {
                        redirectUri = currentUri.resolve(redirectUri);
                    }

                    redirects.add(redirectUri);
                    logger.debug("Redirecting to: {}", redirectUri);

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
                status = ConnectionStatus.TOO_MANY_REDIRECTS;
                queryContext.addError(-13013, "no response available", "Too many HTTP redirects.");
                queryContext.getConnectionTracker().updateCurrentConnection(status);
            }

            // if we exit the loop without a redirect, we have a final response
            httpResponse = response;
        } catch (Exception e) {
            logger.debug("Exception when making HTTP request", e);
        }
    }

    /**
     * Validates the HTTP response according to RDAP specifications.
     *
     * <p>This method performs comprehensive validation including status code checks,
     * Content-Type header validation, JSON parsing, and RDAP-specific structure
     * validation. All validation errors are automatically added to the results file.</p>
     */
    private void validate() {
        // Handle the case where we need to add an error for non-200/404 status codes regardless of query success
        boolean isValidStatusCode =
            httpResponse != null && List.of(HTTP_OK, HTTP_NOT_FOUND).contains(httpResponse.statusCode());

        if (!isValidStatusCode) {
            String statusValue =
                httpResponse == null ? "no response available" : String.valueOf(httpResponse.statusCode());
            queryContext.addError(-13002, statusValue, "The HTTP status code was neither 200 nor 404.");
        }

        // Check 2024 profile validations BEFORE early termination to ensure -12108 precedence
        if (isQuerySuccessful() && httpResponse != null && config.useRdapProfileFeb2024()) {
            int httpStatusCode = httpResponse.statusCode();
            String rdapResponse = httpResponse.body();

            if (httpStatusCode != HTTP_OK) {
                if (!validateIfContainsErrorCode(httpStatusCode, rdapResponse)) {
                    queryContext.addError(-12107, rdapResponse, "The errorCode value is required in an error response.");
                    isQuerySuccessful = false;
                } else if (!validateErrorCodeMatchesHttpStatus(httpStatusCode, rdapResponse)) {
                    queryContext.addError(-12108, rdapResponse, "The errorCode value does not match the HTTP status code.");
                    isQuerySuccessful = false;
                }
            }
        }

        // Early termination for client errors (4xx) - these indicate issues with the request itself
        // and further validation is not meaningful (but we check -12108 first above)
        if (!isValidStatusCode && httpResponse != null && httpResponse.statusCode() >= 400 && httpResponse.statusCode() < 500) {
            logger.debug("Client error {} detected, stopping validation early", httpResponse.statusCode());
            isQuerySuccessful = false;
            return;
        }

        // If it wasn't successful, early return, we don't need to validate
        if (!isQuerySuccessful() || httpResponse == null) {
            logger.debug("Querying wasn't successful .. don't validate");
            return;
        }

        // else continue on
        int httpStatusCode = httpResponse.statusCode();
        HttpHeaders headers = httpResponse.headers();
        String rdapResponse = httpResponse.body();
        logger.debug("http Status code: {}", httpStatusCode);

        // dump headers
        headers.map().forEach((k, v) -> logger.debug("Header: {} = {}", k, v));
        // If a response is available to the tool, and the header Content-Type is not
        // application/rdap+JSON, error code -13000 added in results file.
        if (Arrays.stream(String.join(SEMI_COLON, headers.allValues(CONTENT_TYPE)).split(SEMI_COLON))
                  .noneMatch(s -> s.equalsIgnoreCase(APPLICATION_RDAP_JSON))) {
            queryContext.addError(-13000, headers.firstValue(CONTENT_TYPE).orElse("missing"),
                "The content-type header does not contain the application/rdap+json media type.");
        }

        // If a response is available to the tool, but it's not syntactically valid JSON object, error code -13001 added in results file.
        jsonResponse = new JsonData(rdapResponse);
        if (!jsonResponse.isValid()) {
            queryContext.addError(-13001, "response body not given", "The response was not valid JSON.");
            isQuerySuccessful = false;
        }
    }

    /**
     * Checks if query parameters are being blindly copied into redirect Location headers.
     *
     * <p>This security validation ensures that servers don't inappropriately copy
     * query parameters from the original request into redirect Location headers,
     * which could lead to security vulnerabilities.</p>
     *
     * @param headers the HTTP response headers to check
     * @return true if query parameters are being copied inappropriately, false otherwise
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
                queryContext.addError(-13004, "<location header value>",
                    "Response redirect contained query parameters copied from the request.");
                return true;
            }
        }
        return false; // not copying them, so don't worry
    }

    /**
     * Validates if the JSON response has a valid RDAP structure.
     *
     * <p>This method checks for the presence of required objectClassName properties
     * in the top-level object and recursively validates nested structures like
     * entities and nameservers according to RDAP specifications.</p>
     *
     * @return true if the JSON response has valid RDAP structure, false otherwise
     */
    public boolean jsonResponseValid() {
        if (jsonResponse == null) {
            return false;
        }

        boolean objectClassExists = true;
        if (!jsonResponse.hasKey("objectClassName")) {
            logger.debug("Validating objectClass property in top level");
            objectClassExists = false;
        }

        Object entitiesObj = jsonResponse.getValue(ENTITIES);
        if (objectClassExists && entitiesObj instanceof List<?> entities) {
            logger.debug("Validating objectClass property in entities");
            objectClassExists = verifyIfObjectClassPropExits(entities, ENTITIES);
            if (objectClassExists) {
                objectClassExists = verifyIfObjectClassPropExits(entities, AUTNUMS);
            }

            if (objectClassExists) {
                objectClassExists = verifyIfObjectClassPropExits(entities, NETWORKS);
            }
        }

        Object nameserversObj = jsonResponse.getValue(NAMESERVERS);
        if (objectClassExists && nameserversObj instanceof List<?> nameservers) {
            logger.debug("Validating objectClass property in nameservers");
            objectClassExists = verifyIfObjectClassPropExits(nameservers, NAMESERVERS);
        }

        return null != jsonResponse && objectClassExists;
    }

    /**
     * Checks if the JSON response contains a nameserver search results collection.
     *
     * <p>This method validates that the response has the required nameserverSearchResults
     * structure for nameserver search queries according to RDAP specifications.</p>
     *
     * @return true if the response contains a valid nameserverSearchResults collection, false otherwise
     */
    boolean hasNameserverSearchResults() {
        return null != jsonResponse && jsonResponse.hasKey(NAMESERVER_SEARCH_RESULTS) && jsonResponse.getValue(
            NAMESERVER_SEARCH_RESULTS) instanceof Collection<?>;
    }


    /**
     * Inner class for parsing and accessing JSON data from RDAP responses.
     *
     * <p>This class provides a wrapper around JSON parsing that can handle both
     * JSON objects (maps) and JSON arrays (lists) commonly found in RDAP responses.
     * It uses Jackson ObjectMapper for parsing and provides convenience methods
     * for accessing data and validating structure.</p>
     */
    public static class JsonData {

        private Map<String, Object> rawRdapMap = null;
        private List<Object> rawRdapList = null;

        /**
         * Creates a new JsonData instance by parsing the provided JSON string.
         *
         * <p>Attempts to parse the data as a JSON object first, and if that fails,
         * tries to parse it as a JSON array. If both parsing attempts fail, the
         * JsonData will be marked as invalid.</p>
         *
         * @param data the JSON string to parse
         */
        public JsonData(String data) {
            ObjectMapper mapper = org.icann.rdapconformance.validator.workflow.JsonMapperUtil.getSharedMapper();

            try {
                rawRdapMap = mapper.readValue(data, Map.class);
            } catch (Exception e1) {
                // JSON content may be a list
                try {
                    rawRdapList = mapper.readValue(data, List.class);
                } catch (Exception e2) {
                    logger.debug("Invalid JSON in RDAP response");
                }
            }
        }

        /**
         * Checks if the JSON data was successfully parsed.
         *
         * @return true if either a JSON object or array was successfully parsed, false otherwise
         */
        public boolean isValid() {
            return rawRdapMap != null || rawRdapList != null;
        }

        /**
         * Checks if the parsed JSON data represents an array.
         *
         * @return true if the JSON data is an array, false if it's an object or invalid
         */
        public boolean isArray() {
            return rawRdapList != null;
        }

        /**
         * Checks if the JSON object contains the specified key.
         *
         * <p>This method only works for JSON objects (not arrays). Returns false
         * if the data is invalid, an array, or doesn't contain the key.</p>
         *
         * @param key the key to check for
         * @return true if the JSON object contains the key, false otherwise
         */
        public boolean hasKey(String key) {
            return isValid() && !isArray() && rawRdapMap.containsKey(key);
        }

        /**
         * Retrieves the value associated with the specified key from the JSON object.
         *
         * <p>This method only works for JSON objects (not arrays). Returns null
         * if the key doesn't exist or if the data is not a valid JSON object.</p>
         *
         * @param key the key to retrieve the value for
         * @return the value associated with the key, or null if not found
         */
        public Object getValue(String key) {
            return rawRdapMap.get(key);
        }
    }

    /**
     * Recursively verifies that all objects in a collection have the objectClassName property.
     *
     * <p>This method validates that each object in the provided collection contains the
     * required objectClassName property, and recursively checks any nested collections
     * with the specified key for the same requirement.</p>
     *
     * @param propertyCollection the collection of objects to validate
     * @param containedKey the key to check for nested collections that also need validation
     * @return true if all objects have objectClassName properties, false otherwise
     */
    public boolean verifyIfObjectClassPropExits(List<?> propertyCollection, String containedKey) {
        for (var x : propertyCollection) {
            if (!(x instanceof Map<?, ?> map)) {
                return false;
            }
            var value = map.get(OBJECT_CLASS_NAME);
            if (value == null) {
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

    /**
     * Validates if an RDAP error response contains the required errorCode field.
     *
     * <p>According to RDAP specifications, error responses must contain both errorCode
     * and rdapConformance fields. This method validates that requirement for non-200
     * HTTP status codes.</p>
     *
     * @param httpStatusCode the HTTP status code from the response
     * @param rdapResponse the JSON response body to validate
     * @return true if the response is valid (200 status or contains required error fields), false otherwise
     */
    public boolean validateIfContainsErrorCode(int httpStatusCode, String rdapResponse) {
        if (httpStatusCode == HTTP_OK) {
            return true; // obviously we don't have to check then, pass
        }

        if (rdapResponse == null || rdapResponse.isBlank()) {
            logger.debug("Empty response body for HTTP status code: {}", httpStatusCode);
            return false;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(rdapResponse, new TypeReference<Map<String, Object>>() {
            });
            return responseMap.containsKey(ERROR_CODE) && responseMap.containsKey(RDAP_CONFORMANCE);
        } catch (Exception e) {
            logger.debug("Error parsing JSON response for error code check", e);
            return false;
        }
    }

    /**
     * Determines if the given HTTP status code represents a redirect response.
     *
     * <p>This method checks for standard HTTP redirect status codes including:
     * 301 (Moved Permanently), 302 (Found), 303 (See Other), 307 (Temporary Redirect),
     * and 308 (Permanent Redirect).</p>
     *
     * @param status the HTTP status code to check
     * @return true if the status code indicates a redirect, false otherwise
     */

    public boolean validateErrorCodeMatchesHttpStatus(int httpStatusCode, String rdapResponse) {
        if (httpStatusCode == HTTP_OK) {
            return true; // No need to check errorCode for successful responses
        }

        if (rdapResponse == null || rdapResponse.isBlank()) {
            logger.info("Empty response body for HTTP status code: {}", httpStatusCode);
            return false;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responseMap = mapper.readValue(rdapResponse, new TypeReference<Map<String, Object>>() {
            });
            
            Object errorCodeObj = responseMap.get(ERROR_CODE);
            if (errorCodeObj == null) {
                // If errorCode doesn't exist, we can't validate it matches
                // This case is handled by the -12107 validation
                return true;
            }
            
            // Convert errorCode to integer for comparison
            int errorCode;
            if (errorCodeObj instanceof Number) {
                errorCode = ((Number) errorCodeObj).intValue();
            } else if (errorCodeObj instanceof String) {
                errorCode = Integer.parseInt((String) errorCodeObj);
            } else {
                logger.info("Invalid errorCode type in response: {}", errorCodeObj.getClass());
                return false;
            }
            
            return errorCode == httpStatusCode;
        } catch (Exception e) {
            logger.info("Error parsing JSON response for error code match check", e);
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