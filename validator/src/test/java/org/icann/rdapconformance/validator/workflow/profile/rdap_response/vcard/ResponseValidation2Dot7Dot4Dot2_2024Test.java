package org.icann.rdapconformance.validator.workflow.profile.rdap_response.vcard;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot4Dot2_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot4Dot2_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_org.json",
            "rdapResponseProfile_2_7_4_2_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot4Dot2_2024(
            jsonObject.toString(),
            results);
    }

    @Test
    public void test63300() {
        jsonObject.getJSONArray("redacted").remove(0);
        validate(-63300, "#/redacted/0:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"emptyValue\",\"name\":{\"type\":\"Registrant Name\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\",\"pathLang\":\"jsonpath\"}, #/redacted/1:{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Tech Phone\"},\"prePath\":\"$.entities[?(@.roles[0]=='technical')].vcardArray[1][?(@[1].type=='voice')]\"}",
            "a redaction of type Registrant Organization is required.");
    }

    @Test
    public void test63301() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "book.[");
        validate(-63301, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book.[\"}",
            "jsonpath is invalid for Registrant Organization");
    }

    @Test
    public void test63302() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]");
        validate(-63302, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='fn')][3]\"}",
            "jsonpath must evaluate to a zero set for redaction by removal of Registrant Organization.");
    }

    @Test
    public void test63303() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-63303, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Registrant Organization\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}",
            "Registrant Organization redaction method must be removal if present");
    }
}
