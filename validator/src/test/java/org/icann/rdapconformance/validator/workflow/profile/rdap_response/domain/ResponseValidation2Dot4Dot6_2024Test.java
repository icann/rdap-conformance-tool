package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot4Dot6_2024Test extends ResponseDomainValidationTestBase {

    public ResponseValidation2Dot4Dot6_2024Test() {
        super("rdapResponseProfile_2_4_6_Validation");
    }

    @BeforeMethod
    public void setup() throws Exception {
        JSONObject link = new JSONObject();
        link.put("href", "https://icann.org/wicf");
        link.put("value", "https://example.com/");
        link.put("rel", "about");
        link.put("type", "text/html");
        JSONArray links = new JSONArray();
        links.put(link);

        jsonObject
            .getJSONArray("entities")
            .getJSONObject(0)
            .put("links", links);
    }


    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot4Dot6_2024(jsonObject.toString(), results, datasets, queryType);
    }

    @Test
    @Override
    public void testValidate_ok() {
        validate();
    }

    @Test
    public void testValidate_NoLinkWithRelIsAbout_AddResults47700() throws Exception {
        removeKey("$['entities'][0]['links']");
        validate(-47700,
            "#/entities/0:{\"objectClassName\":\"entity\",\"publicIds\":[{\"identifier\":\"292\",\"type\":\"IANA Registrar ID\"}],\"vcardArray\":[\"vcard\","
                + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"Example Inc.\"]]],\"entities\":[{\"objectClassName\":\"entity\",\"vcardArray\":[\"vcard\","
                + "[[\"version\",{},\"text\",\"4.0\"],[\"fn\",{},\"text\",\"\"],[\"tel\",{\"type\":\"voice\"},\"uri\",\"tel:+1.9999999999\"],"
                + "[\"email\",{},\"text\",\"abusecomplaints@example.com\"],[\"adr\",{\"type\":\"work\"},\"text\",[\"\",\"Suite 1234\",\"4321 Rue Somewhere\","
                + "\"Quebec\",\"QC\",\"G1V 2M2\",\"\"]]]],\"roles\":[\"abuse\"],\"handle\":\"292\"}],\"roles\":[\"registrar\"],\"handle\":\"292\"}",
            "A domain must have link to the RDAP base URL of the registrar.");
    }

    @Test
    public void testValidate_HrefNotHttps_AddResults47702() {
        replaceValue("$['entities'][0]['links'][0]['value']", "http://localhost/");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\",\"value\":\"http://localhost/\"}",
            "The registrar RDAP base URL must have an https scheme.");
    }

    @Test
    public void testValidate_HrefNotRDAPBaseURL_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['value']", "https://localhost/");
        validate(-47702,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\",\"value\":\"https://localhost/\"}",
            "The registrar base URL is not registered with IANA.");
    }
}
