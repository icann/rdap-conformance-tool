package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResponseValidation2Dot7Dot4Dot9_2024Test extends ProfileJsonValidationTestBase {

    static final String voicePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String prePathExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.redacted[*]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot9_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_contact_email.json",
                "rdapResponseProfile_2_7_4_9_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot9_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot8_2024_63700() {
        JSONArray telValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        telValue.put(1, new JSONObject());
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63700, voicePointer, "a redaction of type Registrant Phone is required.");
    }

    @Test
    @Ignore
    public void ResponseValidation2Dot7Dot4Dot8_2024_63701_By_PathLang_NotValid() {
        JSONArray telValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        telValue.put(1, new JSONObject());
        redactedObject.put("prePath", "$test");
        validate(-63701, pathLangBadPointer, "jsonpath is invalid for Registrant Phone.");
    }

    @Test
    @Ignore
    public void ResponseValidation2Dot7Dot4Dot8_2024_63702_By_MissingPathLang_Bad_PrePath() {
        JSONArray telValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        telValue.put(1, new JSONObject());
        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "$.redacted[*]");
        validate(-63702, prePathExistingPointer, "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone.");
    }

    @Test
    @Ignore
    public void ResponseValidation2Dot7Dot4Dot8_2024_63703_By_Method() {
        JSONArray telValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        telValue.put(1, new JSONObject());
        redactedObject.put("method", "test2");
        validate(-63703, methodPointer, "Registrant Phone redaction method must be removal if present");
    }
}
