package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** * Validates that every jCard with an "adr" property has a valid * ISO 3166-1 alpha-2 "cc" parameter. If no "adr" property exists, * validation is skipped. Error code -62101. */
public class ResponseValidation1Dot2_4_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation1Dot2_4_2024.class);
    private static final Set<String> ISO_3166_1_ALPHA2_CODES = Set.of(Locale.getISOCountries());

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

            for (int i = 0; i < vcard.length(); i++) {
                JSONArray property = (JSONArray) vcard.get(i);
                String propertyName = property.get(0).toString();

                if (ADR_PROPERTY.equals(propertyName)) {
                    // Found an adr — now check for valid cc
                    boolean hasValidCc = false;
                    Object params = property.get(1);
                    if (params instanceof JSONObject paramsObj) {
                        if (paramsObj.has(CC_PARAM)) {
                            String ccValue = paramsObj.getString(CC_PARAM).trim();
                            if (isValidIso3166Alpha2(ccValue)) {
                                hasValidCc = true;
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
                        break; // no need to check more adr properties in this jCard
                    }
                }
            }
        }

        return isValid;
    }

    /**
     * Returns true if the value is a valid ISO 3166-1 alpha-2 country code,
     * validated against the JDK's built-in ISO country list (not just shape checking).
     */
    private boolean isValidIso3166Alpha2(String value) {
        if (value == null || value.length() != 2) {
            return false;
        }
        return ISO_3166_1_ALPHA2_CODES.contains(value.toUpperCase(Locale.ROOT));
    }
}
