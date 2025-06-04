package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot6Dot3_2024Test extends ProfileJsonValidationTestBase {
    public ResponseValidation2Dot7Dot6Dot3_2024Test() {
        super("/validators/profile/response_validations/vcard/tech_email_with_both_emai_and_contact-uri.json",
            "rdapResponseProfile_2_7_6_3_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot7Dot6Dot3_2024(
            jsonObject.toString(),
            results);
    }

    @Test
    public void test65200() {
        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);

        validate(-65200, "[\"vcard\",[[\"email\",{\"type\":\"work\"},\"text\",\"privacy@dnic.JewellaPrivacy.com\"],[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]],[\"contact-uri\",{},\"uri\",\"https://email.example.com/123\"]]]",
            "a redaction of Tech Email may not have both the email and contact-uri");
    }

    @Test
    public void test65201() {
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);
        validate(-65201, "[\"vcard\",[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],[\"org\",{},\"text\",\"Example Inc.\"],[\"adr\",{},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]]]",
            "a redaction of Tech Email must have either email or contact-uri");
    }

    @Test
    public void test65202() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("method", "dummy");
        validate(-65202, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"dummy\",\"name\":{\"type\":\"Tech Email\"}}",
            "Tech Email redaction method must be replacementValue");
    }

    @Test
    public void test65203() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("postPath", "$.store.book.[");
        validate(-65203, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"postPath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Tech Email postPath");
    }

    @Test
    public void test65204() {
        jsonObject.getJSONArray("redacted").getJSONObject(0).put("postPath", "$.store.book[");
        validate(-65204, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"postPath\":\"$.store.book[\"}",
            "jsonpath must evaluate to a non-empty set for redaction by postPath of Tech Email.");
    }

    @Test
    public void test65205() {
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);

        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);


        jsonObject.getJSONArray("redacted").getJSONObject(0).put("replacementPath", "$.store.book.[");
        validate(-65205, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"replacementPath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Tech Email replacementPath");
    }

    @Test
    public void test65207() {
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);

        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);


        jsonObject.getJSONArray("redacted").getJSONObject(0).put("replacementPath", "$.store.book[");
        validate(-65207, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"replacementPath\":\"$.store.book[\"}",
            "jsonpath must evaluate to a non-empty set for redaction by replacementValue of Tech Email in replacementPath");
    }

    @Test
    public void test65206() {
        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).remove(0);

        JSONArray contactUriEntry = new JSONArray();
        contactUriEntry.put("contact-uri");
        contactUriEntry.put(new JSONObject());  // empty JSON object
        contactUriEntry.put("uri");
        contactUriEntry.put("https://email.example.com/123");

        jsonObject.getJSONArray("entities").getJSONObject(0).getJSONArray("vcardArray").getJSONArray(1).put(contactUriEntry);


        jsonObject.getJSONArray("redacted").getJSONObject(0).put("prePath", "$.store.book.[");
        validate(-65206, "{\"reason\":{\"description\":\"Server policy\"},\"method\":\"replacementValue\",\"name\":{\"type\":\"Tech Email\"},\"prePath\":\"$.store.book.[\"}",
            "jsonpath is invalid for Tech Email prePath");
    }
}
