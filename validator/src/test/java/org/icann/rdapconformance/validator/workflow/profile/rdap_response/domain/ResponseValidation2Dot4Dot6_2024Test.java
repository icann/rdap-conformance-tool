package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.mockito.MockedStatic;
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
        super.setUp();
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
        doReturn(new URI("https://icann.org/wicf")).when(queryContext.getConfig()).getUri();
    }


    @Override
    public ProfileValidation getProfileValidation() {
        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        return new ResponseValidation2Dot4Dot6_2024(domainContext);
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

    @Test
    public void testValidate_ReservedRegistrarId9999_GtldRegistryMode_Passes() {
        // Test: Reserved registrar ID 9999 should pass when gTLD registry mode is enabled
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "9999");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "9999");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://any-invalid-url.com/");
        validate();  // Should pass without -47701 error
    }

    @Test
    public void testValidate_ReservedRegistrarId9994_GtldRegistryMode_Passes() {
        // Test: Reserved registrar ID 9994 should pass when gTLD registry mode is enabled
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "9994");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "9994");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://any-invalid-url.com/");
        validate();  // Should pass without -47701 error
    }

    @Test
    public void testValidate_ReservedRegistrarId9995_GtldRegistryMode_Passes() {
        // Test: Reserved registrar ID 9995 should pass when gTLD registry mode is enabled
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "9995");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "9995");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://any-invalid-url.com/");
        validate();  // Should pass without -47701 error
    }

    @Test
    public void testValidate_ReservedRegistrarId9999_NotGtldRegistryMode_FailsValidation() {
        // Test: Reserved registrar ID 9999 should still validate against IANA when NOT in gTLD registry mode
        doReturn(false).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "9999");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "9999");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://invalid-iana-url.com/");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://invalid-iana-url.com/\"}",
            "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_RegularRegistrarId292_GtldRegistryMode_StillValidatesIANA() {
        // Test: Regular registrar IDs should still validate against IANA even in gTLD registry mode
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        // Keep the default handle "292" and test with invalid URL
        replaceValue("$['entities'][0]['links'][0]['value']", "https://invalid-iana-url.com/");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://invalid-iana-url.com/\"}",
            "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_EdgeCaseId9993_GtldRegistryMode_StillValidatesIANA() {
        // Test: ID 9993 (just below reserved range) should still validate against IANA
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "9993");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "9993");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://invalid-iana-url.com/");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://invalid-iana-url.com/\"}",
            "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_EdgeCaseId10000_GtldRegistryMode_StillValidatesIANA() {
        // Test: ID 10000 (just above reserved range) should still validate against IANA
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "10000");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "10000");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://invalid-iana-url.com/");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://invalid-iana-url.com/\"}",
            "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testIsExcludedRegistrarId_ReservedIds_ReturnsTrue() {
        // Test the isExcludedRegistrarId method directly with reserved IDs
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        assertTrue(validator.isExcludedRegistrarId("9994"));
        assertTrue(validator.isExcludedRegistrarId("9995"));
        assertTrue(validator.isExcludedRegistrarId("9996"));
        assertTrue(validator.isExcludedRegistrarId("9997"));
        assertTrue(validator.isExcludedRegistrarId("9998"));
        assertTrue(validator.isExcludedRegistrarId("9999"));
    }

    @Test
    public void testIsExcludedRegistrarId_NonReservedIds_ReturnsFalse() {
        // Test the isExcludedRegistrarId method directly with non-reserved IDs
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        assertFalse(validator.isExcludedRegistrarId("292"));
        assertFalse(validator.isExcludedRegistrarId("9993"));
        assertFalse(validator.isExcludedRegistrarId("10000"));
        assertFalse(validator.isExcludedRegistrarId("123"));
    }

    @Test
    public void testIsExcludedRegistrarId_EdgeCases_HandlesCorrectly() {
        // Test the isExcludedRegistrarId method with edge cases
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        assertFalse(validator.isExcludedRegistrarId(null));
        assertFalse(validator.isExcludedRegistrarId(""));
        assertFalse(validator.isExcludedRegistrarId("abc"));
        assertTrue(validator.isExcludedRegistrarId("  9999  ")); // Should handle whitespace
    }

    @Test
    public void testValidate_HandleIsNull_AddResults47701() {
        // Test: Null handle should trigger -47701 error
        removeKey("$['entities'][0]['handle']");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_HandleIsNotNumber_AddResults47701() {
        // Test: Non-numeric handle should trigger -47701 error
        replaceValue("$['entities'][0]['handle']", "invalid-handle");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "invalid-handle");
        validate(-47701,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_HrefIsNull_AddResults47703() {
        // Test: Null href should trigger -47703 error
        removeKey("$['entities'][0]['links'][0]['href']");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefHostStartsWithDot_AddResults47703() {
        // Test: Host starting with dot should trigger -47703 error
        replaceValue("$['entities'][0]['links'][0]['href']", "https://.example.com/path");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://.example.com/path\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefHostEndsWithDot_AddResults47703() {
        // Test: Host ending with dot should trigger -47703 error
        replaceValue("$['entities'][0]['links'][0]['href']", "https://example.com./path");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://example.com./path\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefHostContainsSpaces_AddResults47703() {
        // Test: Host containing spaces should trigger -47703 error
        replaceValue("$['entities'][0]['links'][0]['href']", "https://exam ple.com/path");
        validate(-47703,
            "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://exam ple.com/path\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
            "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testDefensiveCodeLinesCoverage_Documentation() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        // Verify normal successful validation path still works
        boolean result = validator.validHrefUri("https://example.com/path", "#/test-valid");
        assertTrue(result);

        // Verify syntax errors are caught (demonstrating these reach earlier validation, not defensive checks)
        boolean result2 = validator.validHrefUri("malformed-uri", "#/test-malformed");
        assertFalse(result2);

        // The test passes, documenting that the defensive lines are present for safety
        // but are not reachable through normal execution paths due to URI.create() behavior
    }

    @Test
    public void testDefensiveLines_UnreachableButImportantDocumentation() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        // Verify the method exists and works correctly for reachable cases
        boolean validResult = validator.validHrefUri("https://example.com/test", "#/test-valid");
        assertTrue(validResult);

        boolean invalidResult = validator.validHrefUri("invalid-uri-format", "#/test-invalid");
        assertFalse(invalidResult);
    }

    @Test
    public void testDefensiveLines_NullScheme_UsingMockedCommonUtils() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        try (MockedStatic<CommonUtils> mockedCommonUtils = mockStatic(CommonUtils.class)) {
            URI mockUri = URI.create("https://example.com/test");

            mockedCommonUtils.when(() -> CommonUtils.createUri(anyString()))
                            .thenReturn(mockUri);
            mockedCommonUtils.when(() -> CommonUtils.getUriScheme(mockUri))
                            .thenReturn(null);  // This triggers the defensive null scheme check
            mockedCommonUtils.when(() -> CommonUtils.getUriHost(mockUri))
                            .thenReturn("example.com");

            boolean result = validator.validHrefUri("https://example.com/test", "#/test-null-scheme");

            // Validation should fail due to null scheme
            assertFalse(result, "Validation should fail when scheme is null");
        }
    }

    @Test
    public void testDefensiveLines_NullHost_UsingMockedCommonUtils() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();
        try (MockedStatic<CommonUtils> mockedCommonUtils = mockStatic(CommonUtils.class)) {
            URI mockUri = URI.create("https://example.com/test");

            mockedCommonUtils.when(() -> CommonUtils.createUri(anyString()))
                            .thenReturn(mockUri);
            mockedCommonUtils.when(() -> CommonUtils.getUriScheme(mockUri))
                            .thenReturn("https");
            mockedCommonUtils.when(() -> CommonUtils.getUriHost(mockUri))
                            .thenReturn(null);  // This triggers the defensive null host check
            boolean result = validator.validHrefUri("https://example.com/test", "#/test-null-host");

            // Validation should fail due to null host
            assertFalse(result, "Validation should fail when host is null");
        }
    }

    @Test
    public void testDefensiveLines_EmptyHost_UsingMockedCommonUtils() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        try (MockedStatic<CommonUtils> mockedCommonUtils = mockStatic(CommonUtils.class)) {
            URI mockUri = URI.create("https://example.com/test");

            mockedCommonUtils.when(() -> CommonUtils.createUri(anyString()))
                            .thenReturn(mockUri);
            mockedCommonUtils.when(() -> CommonUtils.getUriScheme(mockUri))
                            .thenReturn("https");
            mockedCommonUtils.when(() -> CommonUtils.getUriHost(mockUri))
                            .thenReturn("   ");  // Empty/whitespace string triggers defensive check

            boolean result = validator.validHrefUri("https://example.com/test", "#/test-empty-host");

            // Validation should fail due to empty host
            assertFalse(result, "Validation should fail when host is empty/whitespace");
        }
    }

}
