package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
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
        replaceRegistrantEmail("https://whois.xinnet.com/sendemail/xinnet.com");
        validate(-12320, "https://whois.xinnet.com/sendemail/xinnet.com",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    @Test
    public void test12320_EmptyEmail_ShouldFail() {
        replaceRegistrantEmail("");
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

    @Test
    public void test12320_SpaceInLocalPart_ShouldFail() {
        // "user @domain.com" — unquoted space violates RFC 5322 addr-spec dot-atom rules
        replaceRegistrantEmail("user @domain.com");
        validate(-12320, "user @domain.com",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    @Test
    public void test12320_InvalidEmail_InNestedEntity_ShouldFail() {
        // Nested entity: registrar (index 1) > abuse contact (index 0) has email
        JSONArray nestedVcardProperties = jsonObject.getJSONArray("entities")
                .getJSONObject(1)
                .getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1);

        for (int i = 0; i < nestedVcardProperties.length(); i++) {
            JSONArray prop = nestedVcardProperties.getJSONArray(i);
            if ("email".equals(prop.getString(0))) {
                prop.put(3, "not-an-email");
                break;
            }
        }
        updateQueryContextJsonData();

        validate(-12320, "not-an-email",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    @Test
    public void test12320_InvalidEmail_EntityLookupResponse_ShouldFail() {
        // Topmost object IS the entity (entity lookup response)
        jsonObject.remove("entities");
        jsonObject.put("objectClassName", "entity");
        jsonObject.put("vcardArray", new JSONArray()
                .put("vcard")
                .put(new JSONArray()
                        .put(new JSONArray().put("version").put(new org.json.JSONObject()).put("text").put("4.0"))
                        .put(new JSONArray().put("fn").put(new org.json.JSONObject()).put("text").put("Test Entity"))
                        .put(new JSONArray().put("email").put(new org.json.JSONObject()).put("text").put("bad-email"))));
        updateQueryContextJsonData();

        validate(-12320, "bad-email",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    @Test
    public void testValid_EntityLookupResponse_ValidEmail_ShouldPass() {
        // Topmost object IS the entity with a valid email
        jsonObject.remove("entities");
        jsonObject.put("objectClassName", "entity");
        jsonObject.put("vcardArray", new JSONArray()
                .put("vcard")
                .put(new JSONArray()
                        .put(new JSONArray().put("version").put(new org.json.JSONObject()).put("text").put("4.0"))
                        .put(new JSONArray().put("fn").put(new org.json.JSONObject()).put("text").put("Test Entity"))
                        .put(new JSONArray().put("email").put(new org.json.JSONObject()).put("text").put("valid@example.com"))));
        updateQueryContextJsonData();

        validateOk(results);
    }

    @Test
    public void test12320_QuotedStringLocalPart_ShouldFail() {
        // Quoted-string local-parts are out of scope for RDAP registration data
        replaceRegistrantEmail("\"john doe\"@example.com");
        validate(-12320, "\"john doe\"@example.com",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    @Test
    public void test12320_DomainLiteral_ShouldFail() {
        // Domain-literals are out of scope for RDAP registration data
        replaceRegistrantEmail("user@[192.0.2.1]");
        validate(-12320, "user@[192.0.2.1]",
                "Email addresses must adhere to the 'addr-spec' format of RFC 5322 Section 3.4.1");
    }

    private void replaceRegistrantEmail(String emailValue) {
        JSONArray vcardProperties = jsonObject.getJSONArray("entities")
                .getJSONObject(0)
                .getJSONArray("vcardArray")
                .getJSONArray(1);
        for (int i = 0; i < vcardProperties.length(); i++) {
            JSONArray prop = vcardProperties.getJSONArray(i);
            if ("email".equals(prop.getString(0))) {
                prop.put(3, emailValue);
                break;
            }
        }
        updateQueryContextJsonData();
    }
}