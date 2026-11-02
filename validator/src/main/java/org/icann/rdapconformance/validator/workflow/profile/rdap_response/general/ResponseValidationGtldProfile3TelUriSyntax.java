package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.icann.rdapconformance.validator.workflow.profile.rdap_response.general.ResponseValidationGtldProfile3TelUri.isVoiceOrFaxTel;

import java.io.InputStream;
import java.util.Objects;
import java.util.regex.Pattern;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * gTLD Profile v3 conformance: for every jCard "tel" property with a "voice" or "fax"
 * type parameter, the value must be a syntactically valid tel URI (RFC 3966).
 * Emits -66007 otherwise. The RFC 3966 pattern is shared with the jCard schema
 * (jcard.json#/definitions/tel-uri) so there is a single source of truth, and is compiled
 * case-insensitively because RFC 3966 is case-insensitive for the scheme, parameter names,
 * hex digits and domain names.
 *
 * <p>This check is independent of -66006: a voice/fax tel whose value type is not "uri" and
 * whose value is not a tel URI is reported by both codes, since the profile defines them as
 * separate test cases.</p>
 */
public final class ResponseValidationGtldProfile3TelUriSyntax extends ProfileJsonValidation {

    private static final int VALUE = 3;
    static final Pattern TEL_URI = loadTelUriPattern();

    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3TelUriSyntax(QueryContext queryContext) {
        super(queryContext.getRdapResponseData(), queryContext.getResults());
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3TelUriSyntax_Validation";
    }

    @Override
    protected boolean doValidate() {
        boolean isValid = true;
        for (String vcardArrayPointer : getPointerFromJPath("$..vcardArray")) {
            if (!(jsonObject.query(vcardArrayPointer) instanceof JSONArray vcardArray)
                || !(vcardArray.opt(1) instanceof JSONArray properties)) {
                continue; // malformed jCard: reported by -12305
            }
            for (int i = 0; i < properties.length(); i++) {
                if (properties.opt(i) instanceof JSONArray property && isVoiceOrFaxTel(property)
                    && !isTelUri(property.opt(VALUE))) {
                    results.add(RDAPValidationResult.builder()
                        .code(-66007)
                        .value(vcardArrayPointer + "/1/" + i + ":" + property)
                        .message("The value of the tel property is not a valid tel URI according to RFC 3966.")
                        .build(queryContext));
                    isValid = false;
                }
            }
        }
        return isValid;
    }

    static boolean isTelUri(Object value) {
        return value instanceof String s && TEL_URI.matcher(s).matches();
    }

    private static Pattern loadTelUriPattern() {
        try (InputStream is = Objects.requireNonNull(
            ResponseValidationGtldProfile3TelUriSyntax.class.getClassLoader()
                .getResourceAsStream("json-schema/jcard.json"))) {
            return Pattern.compile(new JSONObject(new JSONTokener(is))
                .getJSONObject("definitions").getJSONObject("tel-uri").getString("pattern"),
                Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load tel-uri pattern from jcard.json", e);
        }
    }
}
