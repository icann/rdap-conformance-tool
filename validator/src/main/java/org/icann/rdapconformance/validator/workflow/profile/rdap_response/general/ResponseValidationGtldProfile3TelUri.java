package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gTLD Profile v3 conformance: for every jCard "tel" property with a "voice" or "fax"
 * type parameter, the jCard value type (third element) must be "uri". Emits -66006 otherwise.
 *
 * <p>Looks at every vcardArray in the response, so it covers entity lookups (top-level
 * vcardArray), entities nested in other objects and entity search results.</p>
 */
public final class ResponseValidationGtldProfile3TelUri extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationGtldProfile3TelUri.class);

    private static final String TEL = "tel";
    private static final String URI_VALUE_TYPE = "uri";
    private static final Set<String> PHONE_TYPES = Set.of("voice", "fax");

    private static final int PROPERTY_NAME = 0;
    private static final int PARAMETERS = 1;
    private static final int VALUE_TYPE = 2;

    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3TelUri(QueryContext queryContext) {
        super(queryContext.getRdapResponseData(), queryContext.getResults());
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3TelUri_Validation";
    }

    @Override
    protected boolean doValidate() {
        boolean isValid = true;
        for (String vcardArrayPointer : getPointerFromJPath("$..vcardArray")) {
            if (!(jsonObject.query(vcardArrayPointer) instanceof JSONArray vcardArray)
                || vcardArray.length() < 2
                || !(vcardArray.opt(1) instanceof JSONArray properties)) {
                // Malformed jCard structures are reported by the schema validation (-12305).
                continue;
            }
            for (int i = 0; i < properties.length(); i++) {
                if (properties.opt(i) instanceof JSONArray property && isVoiceOrFaxTel(property)
                    && !URI_VALUE_TYPE.equals(property.opt(VALUE_TYPE))) {
                    String pointer = vcardArrayPointer + "/1/" + i;
                    logger.debug("tel property at {} does not use the uri value type", pointer);
                    results.add(RDAPValidationResult.builder()
                        .code(-66006)
                        .value(pointer + ":" + property)
                        .message("A voice or fax tel property must use the jCard uri value type.")
                        .build(queryContext));
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    private static boolean isVoiceOrFaxTel(JSONArray property) {
        if (!TEL.equals(property.opt(PROPERTY_NAME))
            || !(property.opt(PARAMETERS) instanceof JSONObject parameters)) {
            return false;
        }
        Object type = parameters.opt("type");
        if (type instanceof JSONArray types) {
            for (Object t : types) {
                if (t instanceof String s && PHONE_TYPES.contains(s)) {
                    return true;
                }
            }
            return false;
        }
        return type instanceof String s && PHONE_TYPES.contains(s);
    }
}
