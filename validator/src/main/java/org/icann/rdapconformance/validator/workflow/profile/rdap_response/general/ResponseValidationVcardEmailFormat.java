package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.utils.EmailValidator;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Validates that every "email" property value in every vCardArray of every entity
 * conforms to the addr-spec format defined in RFC 5322 Section 3.4.1.
 * Error code -12320.
 */
public class ResponseValidationVcardEmailFormat extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationVcardEmailFormat.class);
    private static final int CODE = -12320;
    private static final String MESSAGE = "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1";
    private static final String ALL_ENTITIES_PATH = "$.entities[*]";

    private final QueryContext queryContext;
    private final EmailValidator emailValidator;

    public ResponseValidationVcardEmailFormat(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
        this.emailValidator = new EmailValidator();
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_vcard_email_format_Validation";
    }

    @Override
    protected boolean doValidate() {
        Set<String> entityPointers = getPointerFromJPath(ALL_ENTITIES_PATH);
        if (entityPointers == null || entityPointers.isEmpty()) {
            return true;
        }

        boolean isValid = true;

        for (String entityPointer : entityPointers) {
            JSONObject entity = (JSONObject) jsonObject.query(entityPointer);
            JSONArray vcardArray = entity.optJSONArray("vcardArray");

            if (vcardArray == null || vcardArray.length() < 2) {
                continue;
            }

            JSONArray vcardProperties = vcardArray.getJSONArray(1);

            for (int i = 0; i < vcardProperties.length(); i++) {
                try {
                    JSONArray property = vcardProperties.getJSONArray(i);
                    if (property.length() >= 4 && "email".equals(property.getString(0))) {
                        String emailValue = property.get(3).toString();
                        if (!emailValidator.validateEmail(emailValue)) {
                            logger.debug("Invalid email addr-spec: {}", emailValue);
                            results.add(RDAPValidationResult.builder()
                                    .code(CODE)
                                    .value(emailValue)
                                    .message(MESSAGE)
                                    .build(queryContext));
                            isValid = false;
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Skipping malformed vcard property at index {}: {}", i, e.getMessage());
                }
            }
        }

        return isValid;
    }
}