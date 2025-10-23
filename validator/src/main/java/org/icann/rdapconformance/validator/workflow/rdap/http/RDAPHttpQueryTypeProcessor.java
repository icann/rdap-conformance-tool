package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.SchemaValidatorCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPHttpQueryTypeProcessor implements RDAPQueryTypeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQueryTypeProcessor.class);

    // Session-keyed storage for concurrent validation requests
    private static final ConcurrentHashMap<String, RDAPHttpQueryTypeProcessor> sessionInstances = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, RDAPValidatorConfiguration> sessionConfigs = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ToolResult> sessionStatus = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, RDAPHttpQueryType> sessionQueryTypes = new ConcurrentHashMap<>();

    // Instance holds its session ID for accessing the correct session data
    private final String sessionId;

    // Private constructor for singleton
    private RDAPHttpQueryTypeProcessor(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets the singleton instance for a specific session
     *
     * @param sessionId the session identifier
     * @return the singleton instance for this session
     */
    public static synchronized RDAPHttpQueryTypeProcessor getInstance(String sessionId) {
        return sessionInstances.computeIfAbsent(sessionId, k -> {
            // Initialize session data when creating new instance
            sessionConfigs.remove(k); // Ensure clean state
            sessionStatus.remove(k);
            sessionQueryTypes.remove(k);
            return new RDAPHttpQueryTypeProcessor(k);
        });
    }

    /**
     * Gets the singleton instance (deprecated - uses default session)
     *
     * @deprecated Use getInstance(String sessionId) instead
     * @return the singleton instance for default session
     */
    @Deprecated
    public static synchronized RDAPHttpQueryTypeProcessor getInstance() {
        return getInstance("default");
    }

    /**
     * Static method to get the singleton instance with configuration (deprecated)
     *
     * @deprecated Use getInstance(String sessionId) and setConfiguration(String sessionId, config) instead
     */
    @Deprecated
    public static synchronized RDAPHttpQueryTypeProcessor getInstance(RDAPValidatorConfiguration config) {
        RDAPHttpQueryTypeProcessor instance = getInstance("default");
        instance.setConfiguration(config);
        return instance;
    }

    /**
     * Resets the singleton instance for a specific session
     *
     * @param sessionId the session to reset
     */
    public static void reset(String sessionId) {
        sessionInstances.remove(sessionId);
        sessionConfigs.remove(sessionId);
        sessionStatus.remove(sessionId);
        sessionQueryTypes.remove(sessionId);
    }

    /**
     * Resets all sessions (primarily for testing)
     */
    public static void resetAll() {
        sessionInstances.clear();
        sessionConfigs.clear();
        sessionStatus.clear();
        sessionQueryTypes.clear();
    }

    /**
     * Sets the configuration for a specific session
     *
     * @param sessionId the session identifier
     * @param config the validator configuration
     */
    public void setConfiguration(String sessionId, RDAPValidatorConfiguration config) {
        sessionConfigs.put(sessionId, config);
        sessionStatus.remove(sessionId);
        sessionQueryTypes.remove(sessionId);
    }

    /**
     * Method to set the configuration (deprecated - uses default session)
     *
     * @deprecated Use setConfiguration(String sessionId, config) instead
     */
    @Deprecated
    public void setConfiguration(RDAPValidatorConfiguration config) {
        setConfiguration("default", config);
    }

    @Override
    public boolean check(RDAPDatasetService datasetService) {
        RDAPValidatorConfiguration config = sessionConfigs.get(sessionId);
        if (config == null) {
            logger.error("Configuration not set for session {}", sessionId);
            sessionStatus.put(sessionId, ToolResult.CONFIG_INVALID);
            return false;
        }

        RDAPHttpQueryType queryType = RDAPHttpQueryType.getType(config.getUri().toString());

        if (queryType == null) {
            logger.error("Unknown RDAP query type for URI {} in session {}", config.getUri(), sessionId);
            sessionStatus.put(sessionId, ToolResult.UNSUPPORTED_QUERY);
            return false;
        }

        sessionQueryTypes.put(sessionId, queryType);

        if (Set.of(RDAPHttpQueryType.DOMAIN, RDAPHttpQueryType.NAMESERVER).contains(queryType)) {
            String domainName = queryType.getValue(config.getUri().toString());

            // Check for mixed labels first
            if (hasMixedLabels(domainName)) {
                logger.error("Mixed label format detected in domain name: {} in session {}", domainName, sessionId);
                sessionStatus.put(sessionId, ToolResult.MIXED_LABEL_FORMAT);
                return false;
            }

            // Domain validation block - Re-enabled to capture input domain validation errors
            String domainNameJson = String.format("{\"domain\": \"%s\"}", domainName);

            // Store current results count to capture only domain validation errors
            RDAPValidatorResults mainResults = RDAPValidatorResultsImpl.getInstance(sessionId);
            int currentResultCount = mainResults.getResultCount();

            SchemaValidator validator = SchemaValidatorCache.getCachedValidator("rdap_domain_name.json", mainResults, datasetService);
            boolean isValid = validator.validate(domainNameJson);

            if (!isValid) {
                logger.debug("Domain name validation failed for: {} - errors captured in main results", domainName);
                int newResultCount = mainResults.getResultCount();
                logger.debug("Added {} domain validation errors to results", newResultCount - currentResultCount);

                sessionStatus.put(sessionId, ToolResult.BAD_USER_INPUT); // it's not REALLY bad user input, but we need to flag an error so we save the results in the results file. This is a special case.
                // Continue execution (return true) so that domain validation errors are included in final results
                return true;
            }
            return true;
        }

        return true;
    }

    /**
     * Gets the error status for a specific session
     *
     * @param sessionId the session identifier
     * @return the error status for this session
     */
    public ToolResult getErrorStatus(String sessionId) {
        return sessionStatus.get(sessionId);
    }

    /**
     * Gets the error status (deprecated - uses default session)
     *
     * @deprecated Use getErrorStatus(String sessionId) instead
     */
    @Override
    @Deprecated
    public ToolResult getErrorStatus() {
        return getErrorStatus(this.sessionId);
    }

    /**
     * Gets the query type for a specific session
     *
     * @param sessionId the session identifier
     * @return the query type for this session
     */
    public RDAPQueryType getQueryType(String sessionId) {
        RDAPHttpQueryType queryType = sessionQueryTypes.get(sessionId);
        if (queryType == null) {
            logger.error("Query type is null for session {}, check() must be called first or returned false", sessionId);
            return null;
        }
        return queryType.getQueryType();
    }

    /**
     * Gets the query type (deprecated - uses default session)
     *
     * @deprecated Use getQueryType(String sessionId) instead
     */
    @Override
    @Deprecated
    public RDAPQueryType getQueryType() {
        return getQueryType(this.sessionId);
    }

    /**
     * Checks if the domain name contains mixed A-labels and U-labels. A-labels are prefixed with "xn--" and U-labels
     * contain non-ASCII characters. ASCII-only labels (like "example" or "com") are allowed with either type.
     *
     * @param domainName The domain name to check
     * @return true if the domain name contains mixed labels, false otherwise
     */
    public boolean hasMixedLabels(String domainName) {
        if (domainName == null || domainName.isEmpty()) {
            return false;
        }

        String[] labels = domainName.split("\\.");
        boolean hasALabel = false;
        boolean hasULabel = false;

        for (String label : labels) {
            if (label.toLowerCase().startsWith("xn--")) {
                hasALabel = true;
            } else if (!isAscii(label)) {
                hasULabel = true;
            }

            // If we found both types, we have mixed labels
            if (hasALabel && hasULabel) {
                logger.debug("Domain name contains mixed A-labels and U-labels: {}", domainName);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a string contains only ASCII characters.
     *
     * @param s The string to check
     * @return true if the string contains only ASCII characters, false otherwise
     */
    public boolean isAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) {
                return false;
            }
        }
        return true;
    }

    public enum RDAPHttpQueryType {
        DOMAIN(RDAPQueryType.DOMAIN, Pattern.compile("/domain/([^/]+)$")),
        NAMESERVER(RDAPQueryType.NAMESERVER, Pattern.compile("/nameserver/([^/]+)$")),
        ENTITY(RDAPQueryType.ENTITY, Pattern.compile("/entity/([^/]+)$")),
        AUTNUM(RDAPQueryType.AUTNUM, Pattern.compile("/autnum/([^/]+)$")),
        IP(RDAPQueryType.IP_NETWORK, Pattern.compile("/ip/([^/]+)$")),
        HELP(RDAPQueryType.HELP, Pattern.compile("/help$")),
        NAMESERVERS(RDAPQueryType.NAMESERVERS, Pattern.compile("/nameservers\\?ip=([^/]+)$"));

        private final RDAPQueryType queryType;
        private final Pattern pattern;

        RDAPHttpQueryType(RDAPQueryType queryType, Pattern pattern) {
            this.queryType = queryType;
            this.pattern = pattern;
        }

        public static RDAPHttpQueryType getType(String query) {
            for (RDAPHttpQueryType qt : RDAPHttpQueryType.values()) {
                Matcher matcher = qt.pattern.matcher(query);
                if (matcher.find()) {
                    return qt;
                }
            }
            return null;
        }

        RDAPQueryType getQueryType() {
            return this.queryType;
        }

        public String getValue(String query) {
            Matcher matcher = this.pattern.matcher(query);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return EMPTY_STRING;
        }
    }
}
