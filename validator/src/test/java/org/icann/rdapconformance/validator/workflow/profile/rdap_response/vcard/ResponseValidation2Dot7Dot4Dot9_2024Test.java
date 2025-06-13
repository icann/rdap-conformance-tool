package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot7Dot4Dot9_2024Test extends ProfileJsonValidationTestBase {

    static final String vcardPointer =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"administrative.user@example.com\"],[\"contact-uri\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String vcardNoReqPointer =
            "#/entities/0/vcardArray/1:[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"test\",{},\"text\",\"administrative.user@example.com\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]";
    static final String methodNoValidPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"test\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String postPathPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"postPath\":\"$test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String postPathEmptyPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"postPath\":\"$.test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String replacePathPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"replacementPath\":\"$test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String prePathPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";
    static final String replaceEmptyPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Registrant Email\"},\"replacementPath\":\"$.test\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='email')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/2:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Street\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\"}";

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
    public void ResponseValidation2Dot7Dot4Dot9_2024_No_Registrant() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64100() {
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(5);

        vArray.put(0, "contact-uri");
        validate(-64100, vcardPointer, "a redaction of Registrant Email may not have both the email and contact-uri");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64101() {
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);

        vArray.put(0, "test");
        validate(-64101, vcardNoReqPointer, "a redaction of Registrant Email must have either the email and contact-uri");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64102() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("method", "test");
        validate(-64102, methodNoValidPointer, "Registrant Email redaction method must be replacementValue");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64103() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("postPath", "$test");
        validate(-64103, postPathPointer, "jsonpath is invalid for Registrant Email postPath");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64104() {
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        redactedObject.put("postPath", "$.test");
        validate(-64104, postPathEmptyPointer, "jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email.");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64105() {
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        vArray.put(0, "contact-uri");
        redactedObject.put("replacementPath", "$test");
        validate(-64105, replacePathPointer, "jsonpath is invalid for Registrant Email replacementPath");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64106() {
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        vArray.put(0, "contact-uri");
        redactedObject.put("prePath", "$test");
        validate(-64106, prePathPointer, "jsonpath is invalid for Registrant Email prePath");
    }

    @Test
    public void ResponseValidation2Dot7Dot4Dot9_2024_64107() {
        JSONArray vArray = jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).getJSONArray(4);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        vArray.put(0, "contact-uri");
        redactedObject.put("replacementPath", "$.test");
        validate(-64107, replaceEmptyPointer, "jsonpath must evaluate to a non-empty set for redaction by replacementvalue of Registrant Email in replacementPath");
    }
}
