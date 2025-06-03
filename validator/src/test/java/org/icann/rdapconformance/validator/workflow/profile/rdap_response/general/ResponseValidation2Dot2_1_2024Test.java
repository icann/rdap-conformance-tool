package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.ResponseValidation2Dot6Dot3_2024;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot2_1_2024Test extends ProfileJsonValidationTestBase {

    static final String handlePointer =
            "#/handle:2138514test";
    static final String typePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String pathLangPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"test\",\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String pathLangObjectPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":{},\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String pathLangMissingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Domain ID\"},\"prePath\":\"test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test\",\"name\":{\"type\":\"Registry Domain ID\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.handle\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}";

    public ResponseValidation2Dot2_1_2024Test() {
        super("/validators/profile/response_validations/handle/valid.json",
                "rdapResponseProfile_2_2_1_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot2_1_2024(
                jsonObject.toString(),
                results,
                datasets);
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46200() {
        jsonObject.put("handle", "2138514test");
        validate(-46200, handlePointer, "The handle in the domain object does not comply with the format "
                + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46202() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0).getJSONObject("name");

        redactedObject.put("type", "test");
        validate(-46202, typePointer, "a redaction of type Registry Domain ID is required.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46203_By_PathLang() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("pathLang", "test");
        validate(-46203, pathLangPointer, "jsonpath is invalid for Registry Domain ID.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46203_By_PathLangObject() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("pathLang", new JSONObject());
        validate(-46203, pathLangObjectPointer, "jsonpath is invalid for Registry Domain ID.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46203_By_MissingPathLang_Bad_PrePath() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.remove("pathLang");
        redactedObject.put("prePath", "test");
        validate(-46203, pathLangMissingPointer, "jsonpath is invalid for Registry Domain ID.");
    }

    @Test
    public void ResponseValidation2Dot2_1_2024_46204_By_Method() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("method", "test");
        validate(-46204, methodPointer, "Registry Domain ID redaction method must be removal if present");
    }
}
