package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ResponseValidationRedactionDescriptionWarning extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationRedactionDescriptionWarning.class);

    private static final String REDACTED_PATH = "$.redacted[*]";

    private static final String MESSAGE_TEMPLATE =
            "A redaction object with a description of %s exists. " +
                    "This warning may be ignored if the redaction is should not use the 'type' property.";

    // description value → warning code  (add new entries as user stories arrive)
    private static final Map<String, Integer> DESCRIPTION_TO_CODE = new LinkedHashMap<>();
    static {
        DESCRIPTION_TO_CODE.put("Registry Domain ID", -65800);
        // -6580XXX Registry Registrant ID → add when that story arrives ....
    }

    private final RDAPQueryType queryType;
    private final QueryContext queryContext;

    public ResponseValidationRedactionDescriptionWarning(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryType = qctx.getQueryType();
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_redaction_description_warning_Validation";
    }

    @Override
    public boolean doLaunch() {
        return queryType.equals(RDAPQueryType.DOMAIN);
    }

    @Override
    public boolean doValidate() {
        var redactedPointers = getPointerFromJPath(REDACTED_PATH);
        if (redactedPointers == null || redactedPointers.isEmpty()) {
            return true;
        }

        boolean isValid = true;

        for (String pointer : redactedPointers) {
            try {
                JSONObject redacted = (JSONObject) jsonObject.query(pointer);
                JSONObject name = redacted.optJSONObject("name");
                if (name == null) continue;

                Object descriptionObj = name.opt("description");
                if (!(descriptionObj instanceof String description)) continue;

                Integer code = DESCRIPTION_TO_CODE.get(description.trim());
                if (code != null) {
                    results.add(RDAPValidationResult.builder()
                            .code(code)
                            .value(redacted.toString())
                            .message(String.format(MESSAGE_TEMPLATE, description.trim()))
                            .build(queryContext));
                    isValid = false;
                }
            } catch (Exception e) {
                logger.debug("Redacted object at {} is malformed, skipping: {}", pointer, e.getMessage());
            }
        }

        return isValid;
    }
}