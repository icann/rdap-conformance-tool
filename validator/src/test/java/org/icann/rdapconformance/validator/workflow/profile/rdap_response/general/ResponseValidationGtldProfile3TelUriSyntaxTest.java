package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
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

    @Test
    public void testValidate_GlobalNumberWithExtension_Ok() {
        replaceValue(TEL_JPATH, tel("voice", "tel:+1-201-555-0123;ext=1234"));
        validate();
    }

    @Test
    public void testValidate_LocalNumberWithPhoneContext_Ok() {
        replaceValue(TEL_JPATH, tel("fax", "tel:7042;phone-context=example.com"));
        validate();
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
    public void testTelUriPattern_LoadedFromJcardSchema() {
        assertThat(ResponseValidationGtldProfile3TelUriSyntax.isTelUri("tel:+1.9999999999")).isTrue();
        assertThat(ResponseValidationGtldProfile3TelUriSyntax.isTelUri("tel:+1.9999999999 ")).isFalse();
    }
}
