package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static java.net.HttpURLConnection.HTTP_OK;

import java.net.URI;
import java.util.Optional;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResultFile;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;

public class ResponseValidationTestInvalidDomain {

    private static final String TEST_INVALID = "test.invalid";
    private static final String FORWARD_SLASH = "/";

    /**
     * Check if the server is redirecting to itself when TEST_INVALID is checked for
     */
    public static boolean isRedirectingTestDotInvalidToItself(RDAPValidatorResults results,
                                                              URI currentUri,
                                                              URI redirectUri) {
        String currentUriStr = currentUri.toString();
        String redirectUriStr = redirectUri.toString();

        if (currentUriStr.contains(TEST_INVALID) && (redirectUriStr.startsWith(currentUriStr)
            || redirectUriStr.startsWith(FORWARD_SLASH))) {
            results.add(RDAPValidationResult.builder()
                                            .code(-13005)
                                            .value("<location header value>")
                                            .message(
                                                "Server responded with a redirect to itself for domain 'test.invalid'.")
                                            .build());
            return true;
        }
        return false;
    }

    public static boolean isHttpOKAndTestDotInvalid(RDAPValidatorResults results, URI currentUri, int statusCode) {
        String currentUriStr = currentUri.toString();
        if (currentUriStr.contains(TEST_INVALID) && statusCode == HTTP_OK) {
            results.add(RDAPValidationResult.builder()
                                            .code(-13006)
                                            .value(currentUriStr)
                                            .message("Server responded with a 200 Ok for 'test.invalid'.")
                                            .build());
            return true;
        }
        return false;
    }


    /**
     * Check if the user is sending the query and the server is responding with a 200 OK for TEST_INVALID
     */
    public static boolean isHttpOKAndTestDotInvalid(RDAPQuery query, RDAPQueryTypeProcessor queryTypeProcessor, RDAPValidatorResults results, RDAPValidationResultFile rdapValidationResultFile) {
        if (queryTypeProcessor.getQueryType().equals(RDAPQueryType.DOMAIN) && query.getData().contains(TEST_INVALID)) {
            Optional<Integer> statusCode = query.getStatusCode();
            if (statusCode.isPresent() && statusCode.get() == HTTP_OK) {
                rdapValidationResultFile.build(statusCode.get());
                results.add(RDAPValidationResult.builder()
                                                .code(-13006)
                                                .value(query.getData())
                                                .message("Server responded with a 200 Ok for 'test.invalid'.")
                                                .build());
                return true;
            }
        }
        return false;
    }
}
