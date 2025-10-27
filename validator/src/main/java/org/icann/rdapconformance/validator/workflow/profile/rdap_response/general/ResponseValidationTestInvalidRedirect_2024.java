package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.LOCATION;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.SEP;
import static org.icann.rdapconformance.validator.CommonUtils.SLASH;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class ResponseValidationTestInvalidRedirect_2024 extends ProfileValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationTestInvalidRedirect_2024.class);
    public static final int PARTS = 2;
    private final RDAPValidatorConfiguration config;
    private final QueryContext queryContext;
    public static final String DOMAIN_TEST_INVALID_WITH_SLASH = "/domain/test.invalid"; // with the slash

    public ResponseValidationTestInvalidRedirect_2024( RDAPValidatorConfiguration config,
                                                      RDAPValidatorResults results) {
        super(results);
        this.config = config;
        this.queryContext = null; // Legacy constructor for backward compatibility
    }

    public ResponseValidationTestInvalidRedirect_2024(QueryContext queryContext) {
        super(queryContext.getResults());
        this.config = queryContext.getConfig();
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "generalResponseValidation";
    }

    public boolean doValidate() throws Exception {
        if (!canTestForInvalid()) { // if the flags aren't set, we can't test, it's good
            return true;
        }

            logger.debug("Sending a GET request to: {}", createTestInvalidURI());
            HttpResponse<String> response = null;

            if (queryContext != null) {
                // Use QueryContext-aware request for proper IPv6/IPv4 protocol handling
                response = RDAPHttpRequest.makeRequest(queryContext, createTestInvalidURI(), config.getTimeout(), GET);
            } else {
                // Fallback to legacy singleton-based request
                response = RDAPHttpRequest.makeHttpGetRequest(createTestInvalidURI(), config.getTimeout());
            }

            int status = response.statusCode();
            logger.debug("Status code for test.invalid: {}", status);
            if (status == HTTP_OK) { // if it returns a 200 - that is an error
                results.add(RDAPValidationResult.builder()
                                                .queriedURI(response.uri().toString())
                                                .httpMethod(GET)
                                                .httpStatusCode(status)
                                                .code(-13006)
                                                .value(createTestInvalidURI().toString())
                                                .message("Server responded with a 200 OK for 'test.invalid'.")
                                                .build());
                return false;
            } else if (RDAPHttpQuery.isRedirectStatus(status)) {
                return handleRedirect(response);
            }
        return true;
    }

    public boolean handleRedirect(HttpResponse<String> response) {
        String locationHeader = response.headers().firstValue(LOCATION).orElse(EMPTY_STRING);
        logger.debug("Received redirect -> Location header: {}", locationHeader);

        try {
            // Normalize the Location header to a full URL
            URI locationUri = normalizeLocationUri(locationHeader, createTestInvalidURI());
            logger.debug("Normalized Location URI: {}", locationUri);

            // Check if the redirect points to itself
            if (locationUri.equals(createTestInvalidURI())) {
                results.add(RDAPValidationResult.builder()
                                                .queriedURI(locationUri.toString())
                                                .httpStatusCode(response.statusCode())
                                                .httpMethod(GET)
                                                .code(-13005)
                                                .value(locationHeader)
                                                .message("Server responded with a redirect to itself for domain 'test.invalid'.")
                                                .build());
                return false;
            }
        } catch (Exception e) {
            logger.debug("Error normalizing Location header: {}", locationHeader, e);
            return false;
        }

        return true;
    }

    private URI normalizeLocationUri(String locationHeader, URI baseUri) {
        // Strip the base URI's path to its root
        URI strippedBaseUri = URI.create(baseUri.getScheme() + SEP + baseUri.getAuthority() + SLASH);

        // Check if the Location header is a full URL or a relative path
        URI locationUri = URI.create(locationHeader);
        if (!locationUri.isAbsolute()) {
            // Resolve relative URI against the stripped base URI
            locationUri = strippedBaseUri.resolve(locationUri);
        }
        return locationUri;
    }

    public boolean canTestForInvalid() {
        return (this.config.isGtldRegistry() || this.config.isGtldRegistrar()) && this.config.useRdapProfileFeb2024();
    }

    private URI createTestInvalidURI() {
        URI baseURI = extractBaseUri(config.getUri());
        String basePath = baseURI.getPath();

        // Ensure the base path does not end with a slash
        if (basePath.endsWith(SLASH)) {
            basePath = basePath.substring(ZERO, basePath.length() - ONE);
        }

        // Construct the new URI with the appended path
        String newPath = basePath + DOMAIN_TEST_INVALID_WITH_SLASH;
        return URI.create(baseURI.getScheme() + SEP + baseURI.getAuthority() + newPath);
    }

    // Utility method, we may want to move this to a common place
    public static URI extractBaseUri(URI uri) {
        try {
            String path = uri.getPath();
            String[] parts = path.split(SLASH);

            // Ensure there are enough parts to remove
            if (parts.length <= PARTS) {
                throw new IllegalArgumentException("URI path does not have enough parts to extract base: " + uri);
            }

            // Reconstruct the path without the last two parts
            String basePath = String.join(SLASH, Arrays.copyOf(parts, parts.length - PARTS));

            // Reconstruct and return the base URI
            return new URI(uri.getScheme(), uri.getAuthority(), basePath.startsWith(SLASH) ? basePath : SLASH + basePath, null, null);
        } catch (Exception e) {
            // we can't do it, just return the original URI
            return uri;
        }
    }
}


