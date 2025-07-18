package org.icann.rdapconformance.validator.workflow.rdap.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;

import java.security.Security;
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
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

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

    private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQuery.class);

    public RDAPHttpQuery(RDAPValidatorConfiguration config) {
        this.config = config;
    }

    /**
     * Launch the HTTP request and validate it.
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
     * Get the list of redirects
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
                logger.info("objectClassName was not found in the topmost object");
                addErrorToResultsFile(-13003, httpResponse.body(),
                    "The response does not have an objectClassName string.");
                structureValid = false;
            } else if (queryType.equals(RDAPQueryType.NAMESERVERS) && !hasNameserverSearchResults()) {
                logger.info("No JSON array in answer");
                if (config.useRdapProfileFeb2024()) {
                    addErrorToResultsFile(-12610, httpResponse.body(),
                        "The nameserverSearchResults structure is required.");
                    structureValid = false;
                } else {
                    addErrorToResultsFile(-13003, httpResponse.body(),
                        "The response does not have an objectClassName string.");
                    structureValid = false;
                }
            }
        }
        return structureValid;
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


    /**
     * Check if we got errors with the RDAP HTTP request.
     */
    private boolean isQuerySuccessful() {
        return (status == null || status.getCode() == 0) && isQuerySuccessful;
    }

    /**
     * Get the connection status in case of error
     */
    @Override
    public ConformanceError getErrorStatus() {
        return status;
    }

    @Override
    public void setErrorStatus(ConformanceError status) {
        this.status = (ConnectionStatus) status; // remember, 0 is ok - SUCCESS
    }

    public void makeRequest(URI currentUri) {
        try {
            logger.info("Making request to: {}", currentUri); // ensure we log each request
            int remainingRedirects = this.config.getMaxRedirects();
            HttpResponse<String> response = null;

            while (remainingRedirects > ZERO) {
                response = RDAPHttpRequest.makeRequest(currentUri, this.config.getTimeout(), GET, true);
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
                status = ConnectionStatus.TOO_MANY_REDIRECTS;
                addErrorToResultsFile(-13013, "no response available", "Too many HTTP redirects.");
                ConnectionTracker.getInstance().updateCurrentConnection(status);
            }

            // if we exit the loop without a redirect, we have a final response
            httpResponse = response;
        } catch (Exception e) {
            logger.info("Exception when making HTTP request", e);
        }
    }

    private void validate() {
        // Handle the case where we need to add an error for non-200/404 status codes regardless of query success
        boolean isValidStatusCode =
            httpResponse != null && List.of(HTTP_OK, HTTP_NOT_FOUND).contains(httpResponse.statusCode());

        if (!isValidStatusCode) {
            String statusValue =
                httpResponse == null ? "no response available" : String.valueOf(httpResponse.statusCode());
            addErrorToResultsFile(-13002, statusValue, "The HTTP status code was neither 200 nor 404.");
        }

        // If it wasn't successful, early return, we don't need to validate
        if (!isQuerySuccessful() || httpResponse == null) {
            logger.info("Querying wasn't successful .. don't validate");
            return;
        }

        // else continue on
        int httpStatusCode = httpResponse.statusCode();
        HttpHeaders headers = httpResponse.headers();
        String rdapResponse = httpResponse.body();
        logger.info("http Status code: {}", httpStatusCode);

        if (config.useRdapProfileFeb2024()) {
            if (!validateIfContainsErrorCode(httpStatusCode, rdapResponse)) {
                addErrorToResultsFile(-12107, rdapResponse, "The errorCode value is required in an error response.");
            }
        }

        // dump headers
        headers.map().forEach((k, v) -> logger.info("Header: {} = {}", k, v));
        // If a response is available to the tool, and the header Content-Type is not
        // application/rdap+JSON, error code -13000 added in results file.
        if (Arrays.stream(String.join(SEMI_COLON, headers.allValues(CONTENT_TYPE)).split(SEMI_COLON))
                  .noneMatch(s -> s.equalsIgnoreCase(APPLICATION_RDAP_JSON))) {
            addErrorToResultsFile(-13000, headers.firstValue(CONTENT_TYPE).orElse("missing"),
                "The content-type header does not contain the application/rdap+json media type.");
        }

        // If a response is available to the tool, but it's not syntactically valid JSON object, error code -13001 added in results file.
        jsonResponse = new JsonData(rdapResponse);
        if (!jsonResponse.isValid()) {
            addErrorToResultsFile(-13001, "response body not given", "The response was not valid JSON.");
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
                addErrorToResultsFile(-13004, "<location header value>",
                    "Response redirect contained query parameters copied from the request.");
                return true;
            }
        }
        return false; // not copying them, so don't worry
    }

    /* *
    /**
     * Check if the RDAP json response contains a specific key.
     */
    public boolean jsonResponseValid() {
        if (jsonResponse == null) {
            return false;
        }

        boolean objectClassExists = true;
        if (!jsonResponse.hasKey("objectClassName")) {
            logger.info("Validating objectClass property in top level");
            objectClassExists = false;
        }

        Object entitiesObj = jsonResponse.getValue(ENTITIES);
        if (objectClassExists && entitiesObj instanceof List<?> entities) {
            logger.info("Validating objectClass property in entities");
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
            logger.info("Validating objectClass property in nameservers");
            objectClassExists = verifyIfObjectClassPropExits(nameservers, NAMESERVERS);
        }

        return null != jsonResponse && objectClassExists;
    }

    /**
     * Checks if the JSON response contains a nameserver search results collection.
     *
     * @return true if the response contains a valid nameserverSearchResults collection
     */
    boolean hasNameserverSearchResults() {
        return null != jsonResponse && jsonResponse.hasKey(NAMESERVER_SEARCH_RESULTS) && jsonResponse.getValue(
            NAMESERVER_SEARCH_RESULTS) instanceof Collection<?>;
    }


    public static class JsonData {

        private Map<String, Object> rawRdapMap = null;
        private List<Object> rawRdapList = null;


        public JsonData(String data) {
            ObjectMapper mapper = org.icann.rdapconformance.validator.workflow.JsonMapperUtil.getSharedMapper();

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