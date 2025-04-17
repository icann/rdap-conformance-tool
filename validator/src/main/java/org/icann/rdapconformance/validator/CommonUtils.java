package org.icann.rdapconformance.validator;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public class CommonUtils {
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";
    public static final String SLASH = "/";
    public static final String SEP = "://";
    public static final String LOCALHOST = "localhost";
    public static final String LOCAL_IPv4 = "127.0.0.1";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";

    public static final String EMPTY_STRING = "";
    public static final String LOCATION = "Location";

    public static final String APPLICATION_JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    public static final String APPLICATION_JSON_CONTENT_TYPE_RDAP = "application/rdap+json; charset=utf-8";

    public static final int PAUSE = 1000;
    public static final int HTTPS_PORT = 443;
    public static final int HTTP_PORT = 80;
    public static final int ZERO = 0;


    public static void addErrorToResultsFile(RDAPValidatorResults results, int code, String value, String message) {
        results.add(RDAPValidationResult.builder()
                                        .code(code)
                                        .value(value)
                                        .message(message)
                                        .build());
    }
}
