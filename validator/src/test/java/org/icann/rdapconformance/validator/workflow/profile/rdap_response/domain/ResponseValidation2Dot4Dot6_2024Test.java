package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.mockito.Mockito.doReturn;

import java.net.URI;
import java.util.List;
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
        // Updated test setup per co-worker specifications:
        // - value should now contain the IANA RDAP base URL (https://example.com/)
        // - href can be any valid URI
        // - config.getUri() is no longer used for value validation
        JSONObject link = new JSONObject();
        link.put("href", "https://some-valid-uri.com/");  // Changed: href just needs to be valid URI
        link.put("value", "https://example.com/");        // Changed: value now contains IANA base URL
        link.put("rel", "about");
        link.put("type", "text/html");
        JSONArray links = new JSONArray();
        links.put(link);

        jsonObject
            .getJSONArray("entities")
            .getJSONObject(0)
            .put("links", links);

        // Config URI no longer used for value validation in new implementation
        doReturn(new URI("https://icann.org/wicf")).when(config).getUri();
    }


    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot4Dot6_2024(jsonObject.toString(), results, datasets, queryType, config);
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
    public void testValidate_ValueNotIANABaseURL_AddResults47701() {
        // Changed: -47701 now validates that value matches IANA RDAP base URL, not request URL
        replaceValue("$['entities'][0]['links'][0]['value']", "https://localhost/");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://localhost/\"}",
            "The registrar base URL is not registered with IANA.");  // Changed message
    }

    @Test
    public void testValidate_ValueNotHttps_AddResults47702() {
        // Changed: -47702 now validates that value uses HTTPS, not href
        // Use a value that would pass IANA validation but fails HTTPS check
        replaceValue("$['entities'][0]['links'][0]['value']", "ftp://example.com/");
        validate(-47702,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"ftp://example.com/\"}",
            "The registrar RDAP base URL must have an https scheme.");
    }

    @Test
    public void testValidate_HrefNotValidURI_AddResults47703() {
        // Test 1: Invalid URI syntax (equivalent to -10400)
        replaceValue("$['entities'][0]['links'][0]['href']", "invalid-uri");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"invalid-uri\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefInvalidScheme_AddResults47703() {
        // Test 2: Invalid scheme - not http/https (equivalent to -10401)
        replaceValue("$['entities'][0]['links'][0]['href']", "hpsps://example.com/test");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"hpsps://example.com/test\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefFtpScheme_AddResults47703() {
        // Test 3: FTP scheme should fail (not http/https)
        replaceValue("$['entities'][0]['links'][0]['href']", "ftp://example.com/file.txt");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"ftp://example.com/file.txt\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefInvalidHost_AddResults47703() {
        // Test 4: Invalid host format (equivalent to -10402)
        replaceValue("$['entities'][0]['links'][0]['href']", "https://invalid..host.com/test");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://invalid..host.com/test\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefValidHttps_Passes() {
        // Test 5: Valid HTTPS URI should pass
        replaceValue("$['entities'][0]['links'][0]['href']", "https://valid-host.example.com/path");
        validate();  // Should pass without errors
    }

    @Test
    public void testValidate_HrefValidHttp_Passes() {
        // Test 6: Valid HTTP URI should also pass
        replaceValue("$['entities'][0]['links'][0]['href']", "http://valid-host.example.com/path");
        validate();  // Should pass without errors
    }
}
