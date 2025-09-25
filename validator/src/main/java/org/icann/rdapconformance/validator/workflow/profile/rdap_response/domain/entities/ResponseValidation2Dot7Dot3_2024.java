package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

/**
 * 9.4.4 Exclude entities with roles "reseller", "registrar", "registrant", or "technical" validation
 */
public class ResponseValidation2Dot7Dot3_2024 extends HandleValidation {
    public ResponseValidation2Dot7Dot3_2024(RDAPValidatorConfiguration config, String rdapResponse, RDAPValidatorResults results,
        RDAPDatasetService datasetService, RDAPQueryType queryType) {

        super(config, rdapResponse, results, datasetService, queryType, -47600, "entity");
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

            isValid &= validateHandle(jsonPointer + "/handle");
        }
        return isValid;
    }
}
