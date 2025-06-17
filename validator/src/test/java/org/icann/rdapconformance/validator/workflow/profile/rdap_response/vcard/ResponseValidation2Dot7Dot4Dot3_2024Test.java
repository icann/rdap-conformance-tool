package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot4Dot3_2024Test extends ProfileJsonValidationTestBase {

    static final String streetPointer =
            "#/entities/0/vcardArray/1/3:[\"adr\",{},\"text\",[\"\",\"Suite 1236\",3,\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]";
    static final String namePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"test\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$test\",\"pathLang\":\"jsonpath\"}";
    static final String postPathNotExistingPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.status[*]\"}";
    static final String methodPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='org')]\"}, #/redacted/3:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test2\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

    public ResponseValidation2Dot7Dot4Dot3_2024Test() {
        super("/validators/profile/response_validations/vcard/valid.json",
                "rdapResponseProfile_2_7_4_3_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot3_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63400() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);

        streetValue.put(2, 3);
        validate(-63400, streetPointer, "The street value of the adr property is required on the vcard for the registrant.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63401() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63401, namePointer, "a redaction of type Registrant Street is required.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63402_By_PathLang_NotValid() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.put("postPath", "$test");
        validate(-63402, pathLangBadPointer, "jsonpath is invalid for Registrant Street.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63403_By_MissingPathLang_Bad_PrePath() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.remove("pathLang");
        redactedObject.put("postPath", "$.status[*]");
        validate(-63403, postPathNotExistingPointer, "jsonpath must evaluate to non-empty set for redaction by empty value of Registrant Street.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot3_2024_63404_By_Method() {
        JSONArray streetValue = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(3).getJSONArray(3);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(3);

        streetValue.put(2, StringUtils.EMPTY);
        redactedObject.put("method", "test2");
        validate(-63404, methodPointer, "Registrant Street redaction method must be empytValue.");
    }
}
