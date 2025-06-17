package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot4Dot8_2024Test extends ProfileJsonValidationTestBase {

    static final String voicePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String prePathExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.redacted[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot8_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_tel_voice.json",
                "rdapResponseProfile_2_7_4_8_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot8_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63700() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63701_By_PathLang_NotValid() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.put("prePath", "$test");
        validate(-63701, pathLangBadPointer, "jsonpath is invalid for Registrant Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63702_By_MissingPathLang_Bad_PrePath() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "$.redacted[*]");
        validate(-63702, prePathExistingPointer, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63703_By_Method() {
        JSONArray tel = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        tel.remove(1);
        redactedObject.put("method", "test2");
        validate(-63703, methodPointer, "Registrant Phone redaction method must be removal if present");
    }
}
