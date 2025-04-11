package org.icann.rdapconformance.validator;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class CommonUtils {
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";


    public static void addErrorToResultsFile(RDAPValidatorResults results, int code, String value, String message) {
        results.add(RDAPValidationResult.builder()
                                        .code(code)
                                        .value(value)
                                        .message(message)
                                        .build());
    }
}
