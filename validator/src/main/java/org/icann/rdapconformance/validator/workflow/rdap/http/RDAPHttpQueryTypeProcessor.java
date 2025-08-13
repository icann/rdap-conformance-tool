package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.icann.rdapconformance.validator.CommonUtils.EMPTY_STRING;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.SchemaValidatorCache;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPHttpQueryTypeProcessor implements RDAPQueryTypeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQueryTypeProcessor.class);
    private static RDAPHttpQueryTypeProcessor instance;
    private RDAPValidatorConfiguration config;
    private ToolResult status = null;
    private RDAPHttpQueryType queryType = null;

    // Private constructor for singleton
    private RDAPHttpQueryTypeProcessor() {
    }

    public static synchronized RDAPHttpQueryTypeProcessor getInstance() {
        if (instance == null) {
            instance = new RDAPHttpQueryTypeProcessor();
        }
        return instance;
    }
    // Static method to get the singleton instance with configuration
    public static synchronized RDAPHttpQueryTypeProcessor getInstance(RDAPValidatorConfiguration config) {
        if (instance == null) {
            instance = new RDAPHttpQueryTypeProcessor();
        }
        instance.setConfiguration(config);
        return instance;
    }

    // Method to set the configuration
    public void setConfiguration(RDAPValidatorConfiguration config) {
        this.config = config;
        this.status = null;
        this.queryType = null;
    }

    @Override
    public boolean check(RDAPDatasetService datasetService) {
        queryType = RDAPHttpQueryType.getType(this.config.getUri().toString());
        if (queryType == null) {
            logger.error("Unknown RDAP query type for URI {}", this.config.getUri());
            status = ToolResult.UNSUPPORTED_QUERY;
            return false;
        }

        if (Set.of(RDAPHttpQueryType.DOMAIN, RDAPHttpQueryType.NAMESERVER).contains(queryType)) {
            String domainName = queryType.getValue(this.config.getUri().toString());

            // Check for mixed labels first
            if (hasMixedLabels(domainName)) {
                logger.error("Mixed label format detected in domain name: {}", domainName);
                status = ToolResult.MIXED_LABEL_FORMAT;
                return false;
            }

            String domainNameJson = String.format("{\"domain\": \"%s\"}", domainName);
            
            // Store current results count to capture only domain validation errors
            RDAPValidatorResults mainResults = RDAPValidatorResultsImpl.getInstance();
            int currentResultCount = mainResults.getResultCount();
            
            SchemaValidator validator = SchemaValidatorCache.getCachedValidator("rdap_domain_name.json", mainResults, datasetService);
            boolean isValid = validator.validate(domainNameJson);
            
            if (!isValid) {
                logger.info("Domain name validation failed for: {} - errors captured in main results", domainName);
                int newResultCount = mainResults.getResultCount();
                logger.debug("Added {} domain validation errors to results", newResultCount - currentResultCount);
                
                status = ToolResult.BAD_USER_INPUT; // it's not REALLY bad user input, but we need to flag an error so we save the results in the results file. This is a special case.
                // Continue execution (return true) so that domain validation errors are included in final results
                return true;
            }
            return true;
        }

        return true;
    }

    @Override
    public ToolResult getErrorStatus() {
        return this.status;
    }

    @Override
    public RDAPQueryType getQueryType() {
        if (queryType == null) {
            logger.error("Query type is null, check() must be called first or returned false");
            return null;
        }
        return queryType.getQueryType();
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
                logger.error("Domain name contains mixed A-labels and U-labels: {}", domainName);
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
