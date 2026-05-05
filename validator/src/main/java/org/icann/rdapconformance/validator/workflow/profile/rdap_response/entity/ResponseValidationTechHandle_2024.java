package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public final class ResponseValidationTechHandle_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationTechHandle_2024.class);

    public static final String ENTITY_ROLE_PATH = "$.entities[?(@.roles contains 'technical')]";
    public static final String ENTITY_TECH_HANDLE_PATH = "$.entities[?(@.roles contains 'technical')].handle";

    private final RDAPQueryType queryType;
    private final QueryContext queryContext;

    public ResponseValidationTechHandle_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryType = qctx.getQueryType();
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_tech_handle_Validation";
    }

    @Override
    public boolean doLaunch() {
        return queryType.equals(RDAPQueryType.DOMAIN);
    }

    @Override
    public boolean doValidate() {
        // No technical entity at top level — nothing to validate
        if (getPointerFromJPath(ENTITY_ROLE_PATH).isEmpty()) {
            return true;
        }

        // No handle present — nothing to validate (handle is optional)
        if (getPointerFromJPath(ENTITY_TECH_HANDLE_PATH).isEmpty()) {
            return true;
        }

        boolean isValid = true;
        Set<String> entityPointers = getPointerFromJPath(ENTITY_ROLE_PATH);

        for (String jsonPointer : entityPointers) {
            JSONObject entity = (JSONObject) jsonObject.query(jsonPointer);

            if (!entity.has("handle")) {
                continue; // handle absent for this entity, skip
            }

            Object handleObj = entity.get("handle");
            if (handleObj instanceof String handle) {
                if (!handle.matches(CommonUtils.HANDLE_PATTERN)) {
                    results.add(RDAPValidationResult.builder()
                            .code(-65700)
                            .value(handle)
                            .message("The handle of the technical entity does not comply with the format "
                                    + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.")
                            .build(queryContext));
                    isValid = false;
                }
            }
        }

        return isValid;
    }
}