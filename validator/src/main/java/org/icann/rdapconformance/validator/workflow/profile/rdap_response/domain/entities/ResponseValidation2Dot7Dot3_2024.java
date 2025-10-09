package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 9.4.4 Exclude entities with roles "reseller", "registrar", "registrant", or "technical" validation
 *
 * For registrars: Only validates entities that exist in the domain registry (thick registry).
 * This prevents validation errors for entities in thin registries where the registry
 * doesn't have entity data and registrars hold contact information.
 */
public class ResponseValidation2Dot7Dot3_2024 extends HandleValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot3_2024.class);
    private final EntityRegistryLookupService entityLookupService;

    public ResponseValidation2Dot7Dot3_2024(RDAPValidatorConfiguration config, String rdapResponse, RDAPValidatorResults results,
        RDAPDatasetService datasetService, RDAPQueryType queryType) {

        super(config, rdapResponse, results, datasetService, queryType, -47600, "entity");
        this.entityLookupService = new EntityRegistryLookupService(datasetService, config);
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_7_3_validation";
    }

    @Override
    public boolean doLaunch() {
        return queryType.equals(RDAPQueryType.DOMAIN)
            && ((config.isGtldRegistry() && !config.isThin())
            || config.isGtldRegistrar());
    }

    @Override
    protected boolean doValidate() {
        Set<String> entityJsonPointers = getPointerFromJPath("$.entities[?("
            + "        !(@.roles[*] =~ /reseller/) &&"
            + "        !(@.roles[*] =~ /registrar/) &&"
            + "        !(@.roles[*] =~ /registrant/) &&"
            + "        !(@.roles[*] =~ /technical/)"
            + ")]");

        boolean isValid = true;
        for (String jsonPointer : entityJsonPointers) {
            String handlePointer = jsonPointer + "/handle";

            // For registrars: Only validate entities that exist in the domain registry
            if (config.isGtldRegistrar()) {
                String domainName = getDomainName();
                String entityHandle = getEntityHandle(handlePointer);

                if (domainName != null && entityHandle != null) {
                    // Check if entity exists in registry (thick registry check)
                    boolean entityExistsInRegistry = entityLookupService.isEntityInThickRegistry(entityHandle, domainName);

                    if (!entityExistsInRegistry) {
                        logger.debug("Skipping validation for entity {} - not found in registry for domain {}",
                                   entityHandle, domainName);
                        continue; // Skip validation for this entity
                    }

                    logger.debug("Entity {} found in registry for domain {} - proceeding with validation",
                               entityHandle, domainName);
                }
            }

            isValid &= validateHandle(handlePointer);
        }
        return isValid;
    }

    /**
     * Extracts the domain name from the RDAP response.
     * @return Domain name (ldhName or unicodeName), or null if not found
     */
    private String getDomainName() {
        try {
            // Try ldhName first, then unicodeName as fallback
            if (jsonObject.has("ldhName")) {
                return jsonObject.getString("ldhName");
            }

            if (jsonObject.has("unicodeName")) {
                return jsonObject.getString("unicodeName");
            }

            return null;
        } catch (Exception e) {
            logger.debug("Error extracting domain name from response: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extracts the entity handle from the JSON response.
     * @param handlePointer JSON pointer to the handle field (e.g., "#/entities/0/handle")
     * @return Entity handle string, or null if not found
     */
    private String getEntityHandle(String handlePointer) {
        try {
            // Convert JSON pointer to JSONPath for querying
            // e.g., "#/entities/0/handle" -> "$.entities[0].handle"
            String jsonPath = handlePointer.replace("#/", "$.").replace("/", ".");
            if (jsonPath.contains("[") && jsonPath.contains("]")) {
                // Handle array notation - already in correct format
                Object handle = jsonObject.query(jsonPath);
                if (handle != null) {
                    return handle.toString();
                }
            }
            return null;
        } catch (Exception e) {
            logger.debug("Error extracting entity handle from pointer {}: {}", handlePointer, e.getMessage());
            return null;
        }
    }
}
