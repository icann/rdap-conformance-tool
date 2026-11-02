package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3TelUriSyntaxTest extends ProfileJsonValidationTestBase {

    private static final String TEL_JPATH = "$['entities'][0]['entities'][0]['vcardArray'][1][2]";
    private static final String TEL_POINTER = "#/entities/0/entities/0/vcardArray/1/2";
    private static final String MESSAGE =
        "The value of the tel property is not a valid tel URI according to RFC 3966.";

    public ResponseValidationGtldProfile3TelUriSyntaxTest() {
        super("/validators/domain/valid.json", "gtldProfile3TelUriSyntax_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationGtldProfile3TelUriSyntax(queryContext);
    }

    private static List<Object> tel(Object type, Object value) {
        return List.of("tel", Map.of("type", type), "uri", value);
    }

    @DataProvider
    public Object[][] invalidValues() {
        return new Object[][] {
            {"+1.9999999999"},            // missing scheme
            {"tel:"},                     // empty number
            {"tel:+1 999 999 9999"},      // spaces not allowed
            {"tel:abc"},                  // not a number
            {"mailto:a@b.c"},             // wrong scheme
            {"tel:5551234"},              // local number without phone-context
            {42},                         // non-string
        };
    }

    @Test(dataProvider = "invalidValues")
    public void testValidate_InvalidTelUri_AddResult66007(Object value) {
        List<Object> tel = tel("voice", value);
        replaceValue(TEL_JPATH, tel);
        validate(-66007, TEL_POINTER + ":" + new JSONArray(tel), MESSAGE);
    }

    @Test
    public void testValidate_FaxTypeInArray_InvalidValue_AddResult66007() {
        List<Object> tel = tel(List.of("work", "fax"), "not-a-tel-uri");
        replaceValue(TEL_JPATH, tel);
        validate(-66007, TEL_POINTER + ":" + new JSONArray(tel), MESSAGE);
    }

    @DataProvider
    public Object[][] validValues() {
        return new Object[][] {
            {"tel:+1-201-555-0123;ext=1234"},         // global number with extension
            {"tel:7042;phone-context=example.com"},   // local number with phone-context
            // RFC 3966 is case-insensitive for scheme, parameter names, hex digits and domains
            {"TEL:+1-201-555-0123"},
            {"tel:+1-201-555-0123;EXT=1234"},
            {"tel:7042;phone-context=Example.COM"},
            {"tel:+1-201-555-0123;isub=%7e"},
        };
    }

    @Test(dataProvider = "validValues")
    public void testValidate_ValidTelUri_Ok(String value) {
        replaceValue(TEL_JPATH, tel("fax", value));
        validate();
    }

    @Test
    public void testValidate_EntityLookup_TopLevelVcardArray_AddResult66007() throws Exception {
        // Entity lookup response: vcardArray is at the top level, not nested under entities
        jsonObject = new JSONObject(getResource("/validators/entity/valid.json"));
        List<Object> tel = tel("voice", "not-a-tel-uri");
        replaceValue("$['vcardArray'][1][3]", tel);
        validate(-66007, "#/vcardArray/1/3:" + new JSONArray(tel), MESSAGE);
    }

    @Test
    public void testValidate_TextValueTypeWithInvalidValue_Reports66006And66007() {
        // -66006 (value type) and -66007 (RFC 3966 syntax) are independent test cases in
        // gTLD Profile v3, so a single bad tel property is reported by both.
        List<Object> tel = List.of("tel", Map.of("type", "voice"), "text", "+1.999");
        replaceValue(TEL_JPATH, tel);
        updateQueryContextJsonData();

        assertThat(new ResponseValidationGtldProfile3TelUri(queryContext).validate()).isFalse();
        assertThat(getProfileValidation().validate()).isFalse();

        ArgumentCaptor<RDAPValidationResult> captor = ArgumentCaptor.forClass(RDAPValidationResult.class);
        verify(results, times(2)).add(captor.capture());
        assertThat(captor.getAllValues()).extracting(RDAPValidationResult::getCode)
            .containsExactlyInAnyOrder(-66006, -66007);
        assertThat(captor.getAllValues()).extracting(RDAPValidationResult::getValue)
            .containsOnly(TEL_POINTER + ":" + new JSONArray(tel));
    }

    @Test
    public void testValidate_NonVoiceOrFaxTel_InvalidValue_Ignored() {
        replaceValue(TEL_JPATH, tel("cell", "not-a-tel-uri"));
        validate();
    }

    @Test
    public void testValidate_NoVcardArray_Ok() {
        jsonObject.remove("entities");
        validate();
    }

    @Test
    public void testTelUriPattern_IsTheJcardSchemaTelUriPattern() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("json-schema/jcard.json")) {
            String schemaPattern = new JSONObject(new JSONTokener(is))
                .getJSONObject("definitions").getJSONObject("tel-uri").getString("pattern");
            assertThat(ResponseValidationGtldProfile3TelUriSyntax.TEL_URI.pattern()).isEqualTo(schemaPattern);
        }
        assertThat(ResponseValidationGtldProfile3TelUriSyntax.TEL_URI.flags()
            & java.util.regex.Pattern.CASE_INSENSITIVE).isNotZero();
    }
}
