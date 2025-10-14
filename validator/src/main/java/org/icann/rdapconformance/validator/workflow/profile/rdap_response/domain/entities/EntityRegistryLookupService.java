package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.icann.rdapconformance.validator.CommonUtils.DOT;
import static org.icann.rdapconformance.validator.CommonUtils.ENTITY;
import static org.icann.rdapconformance.validator.CommonUtils.GET;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.PAUSE;
import static org.icann.rdapconformance.validator.CommonUtils.SLASH;
import static org.icann.rdapconformance.validator.CommonUtils.TIMEOUT_IN_5SECS;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Set;

/**
 * Service to check if an entity exists in the domain registry by querying the registry directly.
 * This is used to determine if an entity is in a "thick" registry (has the entity data)
 * vs a "thin" registry (registrar holds the entity data).
 *
 * For registrar validation, we only want to enforce EPPROID validation (-47600/-47601)
 * if the entity actually exists in the domain registry (thick registry scenario).
 */
public class EntityRegistryLookupService {

    private static final Logger logger = LoggerFactory.getLogger(EntityRegistryLookupService.class);

    private final RDAPDatasetService datasetService;
    private final RDAPValidatorConfiguration config;

    public EntityRegistryLookupService(RDAPDatasetService datasetService, RDAPValidatorConfiguration config) {
        this.datasetService = datasetService;
        this.config = config;
    }

    /**
     * Checks if an entity exists in the domain registry by making an RDAP entity query.
     *
     * @param entityHandle The entity handle to check (e.g., "12345-EXAMPLE")
     * @param domainName The domain name to extract TLD for bootstrap lookup (e.g., "example.com")
     * @return true if entity exists in registry (200 OK), false if not found or error (404, timeout, etc.)
     */
    public boolean isEntityInThickRegistry(String entityHandle, String domainName) {
        try {
            // Extract TLD from domain name
            String tld = extractTld(domainName);
            if (tld == null) {
                logger.warn("Could not extract TLD from domain: {}", domainName);
                return false;
            }

            logger.info("Starting bootstrap lookup for entity {} in TLD {}", entityHandle, tld);

            // Get registry URLs from bootstrap data
            BootstrapDomainNameSpace bootstrap = datasetService.get(BootstrapDomainNameSpace.class);
            Set<String> registryUrls = bootstrap.getUrlsForTld(tld);

            if (registryUrls == null || registryUrls.isEmpty()) {
                logger.warn("No registry URLs found for TLD: {}", tld);
                return false;
            }

            // Try each registry URL until we get a response
            for (String registryBaseUrl : registryUrls) {
                if (checkEntityInRegistry(entityHandle, registryBaseUrl)) {
                    logger.debug("Entity {} found in registry at {}", entityHandle, registryBaseUrl);
                    return true;
                }
            }

            logger.debug("Entity {} not found in any registry for TLD {}", entityHandle, tld);
            return false;

        } catch (Exception e) {
            logger.warn("Error checking entity {} in registry for domain {}: {}",
                       entityHandle, domainName, e.getMessage());
            return false;
        }
    }

    /**
     * Extracts the TLD from a domain name.
     * @param domainName e.g., "example.com", "test.co.uk", or "example.com." (FQDN)
     * @return TLD e.g., "com" or "co.uk", null if invalid
     */
    private String extractTld(String domainName) {
        if (domainName == null || domainName.trim().isEmpty()) {
            return null;
        }

        String domain = domainName.trim().toLowerCase();

        // Trim trailing dot if present (FQDN)
        if (domain.endsWith(DOT)) {
            domain = domain.substring(ZERO, domain.length() - ONE);
        }

        int lastDot = domain.lastIndexOf('.');
        if (lastDot == -1) {
            return null; // No TLD
        }

        return domain.substring(lastDot + ONE);
    }

    /**
     * Makes an RDAP entity query to a specific registry to check if entity exists.
     * @param entityHandle The entity handle to query
     * @param registryBaseUrl The base URL of the registry (e.g., "https://rdap.verisign.com/com/v1/")
     * @return true if entity found (200 OK), false otherwise
     */
    private boolean checkEntityInRegistry(String entityHandle, String registryBaseUrl) {
        try {
            // Construct entity query URL
            String entityUrl = buildEntityUrl(registryBaseUrl, entityHandle);
            URI entityUri = new URI(entityUrl);

            logger.debug("Checking entity {} at URL: {}", entityHandle, entityUrl);

            // Make HTTP request with short timeout
            java.net.http.HttpResponse<String> response = RDAPHttpRequest.makeRequest(entityUri, TIMEOUT_IN_5SECS / PAUSE, GET);

            // Return true only for 200 OK
            boolean exists = response.statusCode() == HTTP_OK;
            logger.debug("Entity {} check at {}: status={}, exists={}",
                        entityHandle, entityUrl, response.statusCode(), exists);

            return exists;

        } catch (Exception e) {
            logger.debug("Error querying entity {} at registry {}: {}",
                        entityHandle, registryBaseUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Builds the RDAP entity query URL from registry base URL and entity handle.
     * @param registryBaseUrl e.g., "https://rdap.verisign.com/com/v1/"
     * @param entityHandle e.g., "12345-EXAMPLE"
     * @return Full entity URL e.g., "https://rdap.verisign.com/com/v1/entity/12345-EXAMPLE"
     */
    private String buildEntityUrl(String registryBaseUrl, String entityHandle) {
        String baseUrl = registryBaseUrl.trim();

        // Ensure base URL ends with /
        if (!baseUrl.endsWith(SLASH)) {
            baseUrl += SLASH;
        }

        // Add entity path
        return baseUrl + ENTITY + SLASH + entityHandle;
    }
}