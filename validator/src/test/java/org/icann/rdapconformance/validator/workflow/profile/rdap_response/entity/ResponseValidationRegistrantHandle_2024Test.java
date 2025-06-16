package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidationRegistrantHandle_2024Test extends ProfileJsonValidationTestBase {

    static final String handlePointer =
            "#/entities/0:{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Administrative User\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1236\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"email\",{},\"text\",\"administrative.user@example.com\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1-555-555-1236;ext=789\"],[\"tel\",{\"type\":\"fax\"},\"uri\",\"tel:+1-555-555-6321\"]]],\"roles\":[\"registrant\"],\"handle\":\"2138514test\"}";
    static final String namePointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"test\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}";
    static final String pathLangBadPointer =
            "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registry Registrant ID\"},\"prePath\":\"$test\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}";
    public ResponseValidationRegistrantHandle_2024Test() {
        super("/validators/profile/response_validations/entity/valid.json",
                "rdapResponseProfile_registrant_handle_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationRegistrantHandle_2024(
                jsonObject.toString(),
                results,
                datasets);
    }

    @Test
    public void ResponseValidationRegistrationHandle_2024_63100() {
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);

        registrantEntity.put("handle", "2138514test");
        validate(-63100, handlePointer, "The handle of the registrant does not comply with the format (\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void ResponseValidationRegistrationHandle_2024_63102() {
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        registrantEntity.remove("handle");
        redactedObject.getJSONObject("name").put("type", "test");
        validate(-63102, namePointer, "a redaction of type Registry Registrant ID is required.");
    }

    @Test
    public void ResponseValidationRegistrationHandle_2024_63103() {
        JSONObject registrantEntity = jsonObject.getJSONArray("entities").getJSONObject(0);
        JSONObject redactedObject = jsonObject.getJSONArray("redacted").getJSONObject(0);

        registrantEntity.remove("handle");
        redactedObject.put("prePath", "$test");
        validate(-63103, pathLangBadPointer, "jsonpath is invalid for Registry Registrant ID.");
    }
}
