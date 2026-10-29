package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3TelUriTest extends ProfileJsonValidationTestBase {

    private static final String TEL_JPATH = "$['entities'][0]['entities'][0]['vcardArray'][1][2]";
    private static final String TEL_POINTER = "#/entities/0/entities/0/vcardArray/1/2";
    private static final String MESSAGE = "A voice or fax tel property must use the jCard uri value type.";

    public ResponseValidationGtldProfile3TelUriTest() {
        super("/validators/domain/valid.json", "gtldProfile3TelUri_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationGtldProfile3TelUri(queryContext);
    }

    /** Built from plain Lists/Maps so JsonPath.set() serializes it as a JSON array. */
    private static List<Object> tel(Object type, String valueType, String value) {
        return List.of("tel", Map.of("type", type), valueType, value);
    }

    private static String expected(List<Object> tel) {
        return TEL_POINTER + ":" + new JSONArray(tel);
    }

    @Test
    public void testValidate_VoiceTelWithTextType_AddResult66006() {
        List<Object> tel = tel("voice", "text", "+1.9999999999");
        replaceValue(TEL_JPATH, tel);
        validate(-66006, expected(tel), MESSAGE);
    }

    @Test
    public void testValidate_FaxTelWithTextType_AddResult66006() {
        List<Object> tel = tel("fax", "text", "+1.9999999999");
        replaceValue(TEL_JPATH, tel);
        validate(-66006, expected(tel), MESSAGE);
    }

    @Test
    public void testValidate_TypeArrayContainingVoice_TextType_AddResult66006() {
        List<Object> tel = tel(List.of("work", "voice"), "text", "+1.9999999999");
        replaceValue(TEL_JPATH, tel);
        validate(-66006, expected(tel), MESSAGE);
    }

    @Test
    public void testValidate_MissingValueType_AddResult66006() {
        List<Object> tel = List.of("tel", Map.of("type", "voice"));
        replaceValue(TEL_JPATH, tel);
        validate(-66006, expected(tel), MESSAGE);
    }

    @Test
    public void testValidate_TypeArrayContainingFax_UriType_Ok() {
        replaceValue(TEL_JPATH, tel(List.of("work", "fax"), "uri", "tel:+1.9999999999"));
        validate();
    }

    @Test
    public void testValidate_TelWithoutVoiceOrFax_TextType_Ok() {
        // Not in scope of -66006 (covered by -20900)
        replaceValue(TEL_JPATH, tel("cell", "text", "+1.9999999999"));
        validate();
    }

    @Test
    public void testValidate_TelWithoutParameters_Ignored() {
        replaceValue(TEL_JPATH, List.of("tel", "not-an-object", "text", "x"));
        validate();
    }

    @Test
    public void testValidate_TopLevelVcardArray_EntityLookup_AddResult66006() {
        List<Object> tel = tel("voice", "text", "+1.9999999999");
        jsonObject.put("vcardArray", new JSONArray().put("vcard").put(new JSONArray()
            .put(new JSONArray(List.of("version", Map.of(), "text", "4.0")))
            .put(new JSONArray(tel))));
        validate(-66006, "#/vcardArray/1/1:" + new JSONArray(tel), MESSAGE);
    }

    @Test
    public void testValidate_NoVcardArray_Ok() {
        jsonObject.remove("entities");
        validate();
    }
}
