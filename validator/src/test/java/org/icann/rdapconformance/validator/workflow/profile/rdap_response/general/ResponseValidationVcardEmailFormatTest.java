package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidationVcardEmailFormatTest extends ProfileJsonValidationTestBase {

    public ResponseValidationVcardEmailFormatTest() {
        super("/validators/profile/rdapConformance/valid-email.json",
                "rdapResponseProfile_vcard_email_format_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationVcardEmailFormat(queryContext);
    }

    @Test
    public void testValid_ValidEmail_ShouldPass() {
        // Fixture already has a valid email (registrant@example.com) — should pass
        validateOk(results);
    }

    @Test
    public void test12320_UrlInsteadOfEmail_ShouldFail() {
        // Replace the registrant email value (index 3 in the property array) with a URL
        JSONArray vcardProperties = jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1);

        // Find the email property and replace its value
        for (int i = 0; i < vcardProperties.length(); i++) {
            JSONArray prop = vcardProperties.getJSONArray(i);
            if ("email".equals(prop.getString(0))) {
                prop.put(3, "https://whois.xinnet.com/sendemail/xinnet.com");
                break;
            }
        }
        updateQueryContextJsonData();

        validate(-12320, "https://whois.xinnet.com/sendemail/xinnet.com",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    @Test
    public void test12320_EmptyEmail_ShouldFail() {
        JSONArray vcardProperties = jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1);

        for (int i = 0; i < vcardProperties.length(); i++) {
            JSONArray prop = vcardProperties.getJSONArray(i);
            if ("email".equals(prop.getString(0))) {
                prop.put(3, "");
                break;
            }
        }
        updateQueryContextJsonData();

        validate(-12320, "",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    @Test
    public void testValid_NoEntities_ShouldPass() {
        jsonObject.remove("entities");
        updateQueryContextJsonData();

        validateOk(results);
    }

    @Test
    public void testValid_EntityWithoutVcard_ShouldPass() {
        jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .remove("vcardArray");
        updateQueryContextJsonData();

        validateOk(results);
    }
}