package org.icann.rdapconformance.validator;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;

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
    public static final String SEMI_COLON = ";";
    public static final String EMPTY_STRING = "";
    public static final String LOCATION = "Location";
    public static final String CONTENT_TYPE = "Content-Type";

    public static final int PAUSE = 1000;
    public static final int HTTPS_PORT = 443;
    public static final int HTTP_PORT = 80;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int HTTP_NOT_FOUND = 404;


    public static void addErrorToResultsFile(int code, String value, String message) {
        RDAPValidatorResultsImpl.getInstance().add(RDAPValidationResult.builder()
                                                                                    .code(code)
                                                                                    .value(value)
                                                                                    .message(message)
                                                                                    .build());

    }
}
