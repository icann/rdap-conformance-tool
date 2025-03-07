package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 8.8.1.5
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated5 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {
    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot7Dot1DotXAndRelated5.class);

    private final Set<String> roles = new HashSet<>();

    public ResponseValidation2Dot7Dot1DotXAndRelated5(String rdapResponse,
        RDAPValidatorResults results,
        RDAPQueryType queryType,
        RDAPValidatorConfiguration config) {
        super(rdapResponse, results, queryType, config);
    }

    @Override
    protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
        // the parent class EntitiesWithinDomainProfileJsonValidation.doValidate() passes all "entity"
        // in the json of those 4 roles, regardless of "level".
        // need to exclude those are the children of "registrar" role
        // https://icann-jira.atlassian.net/browse/RCT-88
        if (isChildOfRegistrar(jsonPointer)) {
            return true;
        }

        boolean isValid = true;
        if (entity.has("roles")) {
            for (Object role : entity.getJSONArray("roles")) {
                if (!roles.add(role.toString())) {
                    results.add(RDAPValidationResult.builder()
                        .code(-52104)
                        .value(getResultValue(jsonPointer))
                        .message("More than one entity with the following roles were found: "
                            + "registrant, administrative, technical and billing.")
                        .build());
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private boolean isChildOfRegistrar(String jsonPointer) {
        int level = StringUtils.countMatches(jsonPointer, "entities");

        if (level == 1) {
            jsonPointer = jsonPointer + "/roles";

            JSONArray roles = (JSONArray) jsonObject.query(jsonPointer);

            for (Object role : roles) {
                if ("registrar".equalsIgnoreCase(role.toString())) {
                    return true;
                }
            }
        } else if (level > 1) {
            // it is child
            jsonPointer = jsonPointer.substring(0, jsonPointer.lastIndexOf("entities") - 1);

            return isChildOfRegistrar(jsonPointer);
        } else {
            //level < 1, i.e. "entities" is not found in the jsonPointer, should never happen
            logger.warn("level = {}, jsonPointer = {}", level, jsonPointer);
        }

        return false;
    }
}
