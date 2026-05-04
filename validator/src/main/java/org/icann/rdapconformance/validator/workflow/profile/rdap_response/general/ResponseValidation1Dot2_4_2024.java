package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;

import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * Validates that every jCard in every entity has an "adr" property with a valid * ISO 3166-1 alpha-2 "cc" parameter. Error code -62101. */
public class ResponseValidation1Dot2_4_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation1Dot2_4_2024.class);

    private static final String ALL_ENTITIES_PATH = "$.entities[*].vcardArray";
    private static final String ADR_PROPERTY = "adr";
    private static final String CC_PARAM = "cc";

    private final QueryContext queryContext;

    public ResponseValidation1Dot2_4_2024(QueryContext qctx) {
        super(qctx.getRdapResponseData(), qctx.getResults());
        this.queryContext = qctx;
    }

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_1_2_4_Validation";
    }

    @Override
    protected boolean doValidate() {
        Set<String> vcardArrayPointers = getPointerFromJPath(ALL_ENTITIES_PATH);
        if (vcardArrayPointers == null || vcardArrayPointers.isEmpty()) {
            return true;
        }

        boolean isValid = true;

        for (String vcardArrayPointer : vcardArrayPointers) {
            JSONArray vcardArray = (JSONArray) jsonObject.query(vcardArrayPointer);
            if (vcardArray == null || vcardArray.length() < 2) {
                continue;
            }

            JSONArray vcard = (JSONArray) vcardArray.get(1);
            boolean hasValidCc = false;

            for (int i = 0; i < vcard.length(); i++) {
                JSONArray property = (JSONArray) vcard.get(i);
                String propertyName = property.get(0).toString();

                if (ADR_PROPERTY.equals(propertyName)) {
                    // property[1] is the parameters object
                    Object params = property.get(1);
                    if (params instanceof org.json.JSONObject paramsObj) {
                        if (paramsObj.has(CC_PARAM)) {
                            String ccValue = paramsObj.getString(CC_PARAM).trim();
                            if (isValidIso3166Alpha2(ccValue)) {
                                hasValidCc = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!hasValidCc) {
                logger.debug("adding -62101, vcardArray = {}", vcardArray);
                results.add(RDAPValidationResult.builder()
                        .code(-62101)
                        .value(vcardArray.toString())
                        .message("All jCards MUST have an ISO 3166-1 Alpha 2 cc parameter")
                        .build(queryContext));
                isValid = false;
            }
        }

        return isValid;
    }

    /**     * Returns true if the value is a valid ISO 3166-1 alpha-2 country code (2 uppercase letters).     */
    private boolean isValidIso3166Alpha2(String value) {
        if (value == null || value.length() != 2) {
            return false;
        }
        // Must be exactly 2 ASCII letters
        return value.chars().allMatch(Character::isLetter)
                && value.equals(value.toUpperCase(Locale.ROOT));
    }
}
