package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot5Dot1_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot5Dot1_2024Test() {
        super("/validators/profile/response_validations/vcard/valid_phone_ext.json",
            "rdapResponseProfile_2_7_5_1_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot5Dot1_2024(
            jsonObject.toString(),
            results);
    }

    @Test
    public void test63800() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.store.book.[");
        validate(-63800, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone Ext\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Registrant Phone Ext");
    }

    @Test
    public void test63801() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]");
        validate(-63801, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"removal\",\"name\":{\"type\":\"Registrant Phone Ext\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\"}",
            "jsonpath must evaluate to a zero set for redaction by removal of Registrant Phone Ext.");
    }

    @Test
    public void test63802() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-63802, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Registrant Phone Ext\"},\"postPath\":\"$.entities[?(@.roles[0]=='registrant')].vcardArray[1][?(@[0]=='adr')][3][:3]\",\"pathLang\":\"jsonpath\",\"prePath\":\"book\"}",
            "Registrant Phone Ext redaction method must be removal if present");
    }
}

