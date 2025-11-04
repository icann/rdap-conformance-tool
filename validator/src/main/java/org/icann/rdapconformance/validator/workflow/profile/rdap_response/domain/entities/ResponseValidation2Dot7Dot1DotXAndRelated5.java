package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * 8.8.1.5
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated5 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

    private final Set<String> roles = new HashSet<>();
    private final QueryContext queryContext;

    public ResponseValidation2Dot7Dot1DotXAndRelated5(QueryContext queryContext) {
        super(queryContext.getRdapResponseData(), queryContext.getResults(), queryContext.getQueryType(), queryContext.getConfig());
        this.queryContext = queryContext;
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
                    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                        .code(-52104)
                        .value(getResultValue(jsonPointer))
                        .message("More than one entity with the following roles were found: "
                            + "registrant, administrative, technical and billing.");

                    results.add(builder.build(queryContext));
                    isValid = false;
                }
            }
        }

        return isValid;
    }
}
