package org.icann.rdapconformance.validator;

import java.io.InputStream;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParserImpl;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;

import org.slf4j.LoggerFactory;

public class CommonUtils {

    public static final String DOT = ".";
    public static final String HTTP = "http";
    public static final String HTTPS = "https";
    public static final String HTTP_PREFIX = "http://";
    public static final String HTTPS_PREFIX = "https://";
    public static final String SLASH = "/";
    public static final String DOUBLE_SLASH = "//";
    public static final String SEP = "://";
    public static final String LOCALHOST = "localhost";
    public static final String LOCAL_IPv4 = "127.0.0.1";

    public static final String LOCAL_IPv6 = "0000:0000:0000:0000:0000:0000:0000:0001";
    public static final String GET = "GET";
    public static final String HEAD = "HEAD";
    public static final String SEMI_COLON = ";";
    public static final String DASH = "-";
    public static final String EMPTY_STRING = "";
    public static final String LOCATION = "Location";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DOMAIN = "domain";
    public static final String NAMESERVER = "nameserver";
    public static final String AUTNUM = "autnum";
    public static final String ENTITY = "entity";
    public static final String IP = "ip";
    public static final String NAMESERVERS = "nameservers";

    public static final int PAUSE = 1000;
    public static final int TIMEOUT_IN_5SECS = 5000;
    public static final int HTTPS_PORT = 443;
    public static final int HTTP_PORT = 80;
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int HTTP_NOT_FOUND = 404;
    public static final int HTTP_TOO_MANY_REQUESTS = 429;
    public static final String HANDLE_PATTERN = "(\\w|_){1,80}-\\w{1,8}";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static void addErrorToResultsFile(int code, String value, String message) {
        RDAPValidatorResultsImpl.getInstance()
                                .add(RDAPValidationResult.builder().code(code).value(value).message(message).build());

    }

    public static void addErrorToResultsFile(int httpStatusCode, int code, String value, String message) {
        RDAPValidatorResultsImpl.getInstance()
                                .add(RDAPValidationResult.builder()
                                                         .httpStatusCode(httpStatusCode)
                                                         .code(code)
                                                         .value(value)
                                                         .message(message)
                                                         .build());

    }

    public static String replaceQueryTypeInStringWith(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType httpQueryType,
                                                      String originalString,
                                                      String replacementWord) {
        return switch (httpQueryType) {
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.DOMAIN ->
                originalString.replace(SLASH + DOMAIN, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVER ->
                originalString.replace(SLASH + NAMESERVER, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.AUTNUM ->
                originalString.replace(SLASH + AUTNUM, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.ENTITY ->
                originalString.replace(SLASH + ENTITY, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.IP -> originalString.replace(SLASH + IP, replacementWord);
            case RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVERS ->
                originalString.replace(SLASH + NAMESERVERS, replacementWord);
            default -> originalString;
        };
    }


    public static String cleanStringFromExtraSlash(String input) {
        if (input != null) {
            String uriCleaned = input.replaceAll(DOUBLE_SLASH, SLASH);
            if (uriCleaned.endsWith(SLASH)) {
                return input.substring(ZERO, input.length() - ONE);
            }
        }

        return input;
    }

public static RDAPDatasetService initializeDataSet(RDAPValidatorConfiguration config) {
    return initializeDataSet(config, null);
}

public static RDAPDatasetService initializeDataSet(RDAPValidatorConfiguration config, ProgressCallback progressCallback) {
    RDAPDatasetService datasetService = null;
    try {
        datasetService = RDAPDatasetServiceImpl.getInstance(new LocalFileSystem());
        if(!datasetService.download(config.useLocalDatasets(), progressCallback)) {
            return null;
        }
    } catch (SecurityException  | IllegalArgumentException e) {
        logger.error(ToolResult.FILE_READ_ERROR.getDescription());
        System.exit(ToolResult.FILE_READ_ERROR.getCode());
    }
    return datasetService;
}

    public static boolean configFileExists(RDAPValidatorConfiguration config, FileSystem fileSystem) {
        java.net.URI configUri = config.getConfigurationFile();
        String filePath;
        
        // Convert URI to file path for existence check
        if (!configUri.isAbsolute()) {
            filePath = java.nio.file.Path.of(configUri.toString()).toAbsolutePath().toString();
        } else if ("file".equals(configUri.getScheme())) {
            filePath = new java.io.File(configUri).getAbsolutePath();
        } else {
            // For non-file URIs (http, etc), we assume they exist (will fail later if they don't)
            return true;
        }
        
        // Check if file exists (only for local files)
        if (configUri.getScheme() == null || "file".equals(configUri.getScheme())) {
            return fileSystem.exists(filePath);
        }
        
        return true;
    }

    public static ConfigurationFile verifyConfigFile(RDAPValidatorConfiguration config, FileSystem fileSystem) {
        ConfigurationFile configFile = null;
        try (InputStream is = fileSystem.uriToStream(config.getConfigurationFile())) {
            ConfigurationFileParser configParser = new ConfigurationFileParserImpl();
            configFile = configParser.parse(is);
        } catch (Exception e) {
            return null;
        }
        return configFile;
    }
}
