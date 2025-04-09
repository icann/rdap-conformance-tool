package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Arrays;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseValidationTestInvalidRedirect_2024 extends ProfileValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationTestInvalidRedirect_2024.class);
    public static final String SLASH = "/";
    public static final int PARTS = 2;
    public static final String EMPTY_STRING = "";
    public static final String LOCATION = "Location";
    private final RDAPValidatorConfiguration config;
    public static final String DOMAIN_TEST_INVALID_WITH_SLASH = "/domain/test.invalid"; // with the slash
    public static final String SEP = "://";

    public ResponseValidationTestInvalidRedirect_2024( RDAPValidatorConfiguration config,
                                                      RDAPValidatorResults results) {
        super(results);
        this.config = config;
    }

    @Override
    public String getGroupName() {
        return "generalResponseValidation";
    }

    public boolean doValidate() {
        if (!canTestForInvalid()) { // if the flags aren't set, we can't test, it's good
            return true;
        }

        try {
            HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequest(createTestInvalidURI(), config.getTimeout());
            logger.info("Sending a GET request to: {}", createTestInvalidURI());
            int status = response.statusCode();
            logger.info("Status code for test.invalid: {}" , status);
            if (RDAPHttpQuery.isRedirectStatus(status)) {
                return handleRedirect(response);
            }
        } catch (Exception e) {
            logger.error("Exception when making HTTP GET request in [generalResponseValidation]", e);
            return false;
        }

        return true;
    }

    public boolean handleRedirect(HttpResponse<String> response) {
        String locationHeader = response.headers().firstValue(LOCATION).orElse(EMPTY_STRING);
        if (locationHeader.equals(createTestInvalidURI().toString())) {
            results.add(RDAPValidationResult.builder()
                                            .code(-13005)
                                            .value(locationHeader)
                                            .message("Server responded with a redirect to itself for domain 'test.invalid'.")
                                            .build());
            return false;
        }
        return true;
    }

    public boolean canTestForInvalid() {
        return (this.config.isGtldRegistry() || this.config.isGtldRegistrar()) && this.config.useRdapProfileFeb2024();
    }

    public URI createTestInvalidURI() {
        URI baseURI = extractBaseUri(config.getUri());
        String newPath = baseURI.getPath() + DOMAIN_TEST_INVALID_WITH_SLASH;

        // Construct the new URI with the appended path
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


