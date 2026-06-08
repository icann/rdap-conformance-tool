package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import static org.testng.Assert.assertFalse;

import java.net.URI;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.mockito.MockedStatic;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot4Dot6_2024Test extends ResponseDomainValidationTestBase {

    // Allows individual tests to inject a custom dataset service into getProfileValidation()
    private RDAPDatasetService datasetServiceOverride = null;

    public ResponseValidation2Dot4Dot6_2024Test() {
        super("rdapResponseProfile_2_4_6_Validation");
    }

    @BeforeMethod
    public void setup() throws Exception {
        super.setUp();
        JSONObject link = new JSONObject();
        link.put("href", "https://some-valid-uri.com/");
        link.put("value", "https://example.com/");
        link.put("rel", "about");
        link.put("type", "text/html");
        JSONArray links = new JSONArray();
        links.put(link);

        jsonObject
                .getJSONArray("entities")
                .getJSONObject(0)
                .put("links", links);

        doReturn(new URI("https://icann.org/wicf")).when(queryContext.getConfig()).getUri();
    }

    @AfterMethod
    public void resetDatasetOverride() {
        datasetServiceOverride = null;
    }

    /**
     * Helper: creates a Record with status="Reserved" for use in mock setup.
     */
    private RegistrarId.Record createReservedRecord(int id) {
        return new RegistrarId.Record(id, "Reserved Registrar", "", "Reserved", "");
    }

    /**
     * Helper: configures a Mockito-based dataset service that returns a Reserved record
     * for the given id. getProfileValidation() will pick this up via datasetServiceOverride.
     */
    private void useReservedDatasetForId(int id) {
        RegistrarId registrarIdMock = mock(RegistrarId.class);
        doReturn(createReservedRecord(id)).when(registrarIdMock).getById(id);

        RDAPDatasetService ds = mock(RDAPDatasetService.class);
        doReturn(registrarIdMock).when(ds).get(RegistrarId.class);

        this.datasetServiceOverride = ds;
    }

    @Override
    public ProfileValidation getProfileValidation() {
        // Use datasetServiceOverride if set by a test, otherwise fall back to the default
        RDAPDatasetService ds = (datasetServiceOverride != null)
                ? datasetServiceOverride
                : queryContext.getDatasetService();

        QueryContext domainContext = new QueryContext(
                queryContext.getQueryId(),
                queryContext.getConfig(),
                ds,
                queryContext.getQuery(),
                queryContext.getResults(),
                RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(jsonObject.toString());
        return new ResponseValidation2Dot4Dot6_2024(domainContext);
    }

    @Override
    public void testDoLaunch() {
        QueryContext helpContext = new QueryContext(queryContext.getQueryId(),
                queryContext.getConfig(), queryContext.getDatasetService(),
                queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.HELP);
        helpContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot4Dot6_2024(helpContext).doLaunch()).isFalse();

        QueryContext nameserversContext = new QueryContext(queryContext.getQueryId(),
                queryContext.getConfig(), queryContext.getDatasetService(),
                queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVERS);
        nameserversContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot4Dot6_2024(nameserversContext).doLaunch()).isFalse();

        QueryContext nameserverContext = new QueryContext(queryContext.getQueryId(),
                queryContext.getConfig(), queryContext.getDatasetService(),
                queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVER);
        nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot4Dot6_2024(nameserverContext).doLaunch()).isFalse();

        QueryContext entityContext = new QueryContext(queryContext.getQueryId(),
                queryContext.getConfig(), queryContext.getDatasetService(),
                queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.ENTITY);
        entityContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot4Dot6_2024(entityContext).doLaunch()).isFalse();

        QueryContext domainContext = new QueryContext(queryContext.getQueryId(),
                queryContext.getConfig(), queryContext.getDatasetService(),
                queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.DOMAIN);
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot4Dot6_2024(domainContext).doLaunch()).isTrue();
    }

    @Test
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
        replaceValue("$['entities'][0]['links'][0]['value']", "https://localhost/");
        validate(-47701,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://localhost/\"}",
                "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_ValueNotHttps_AddResults47702() {
        replaceValue("$['entities'][0]['links'][0]['value']", "ftp://example.com/");
        validate(-47702,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"ftp://example.com/\"}",
                "The registrar RDAP base URL must have an https scheme.");
    }

    @Test
    public void testValidate_HrefNotValidURI_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['href']", "invalid-uri");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"invalid-uri\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefInvalidScheme_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['href']", "hpsps://example.com/test");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"hpsps://example.com/test\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefFtpScheme_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['href']", "ftp://example.com/file.txt");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"ftp://example.com/file.txt\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefInvalidHost_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['href']", "https://invalid..host.com/test");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://invalid..host.com/test\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefValidHttps_Passes() {
        replaceValue("$['entities'][0]['links'][0]['href']", "https://valid-host.example.com/path");
        validate();
    }

    @Test
    public void testValidate_HrefValidHttp_Passes() {
        replaceValue("$['entities'][0]['links'][0]['href']", "http://valid-host.example.com/path");
        validate();
    }

    // -------------------------------------------------------------------------
    // Tests for -47701: Reserved status check comes from IANA dataset, not hardcoded IDs
    // -------------------------------------------------------------------------

    @Test
    public void testValidate_ReservedStatusInDataset_GtldRegistryMode_Passes() {
        // A record with status="Reserved" in the IANA dataset skips URL validation
        // when running in gTLD registry mode
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "9994");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "9994");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://any-invalid-url.com/");
        useReservedDatasetForId(9994);
        validate();
    }

    @Test
    public void testValidate_ReservedStatusInDataset_AnyId_GtldRegistryMode_Passes() {
        // Any ID (not just 9994-9999) with status="Reserved" is skipped in gTLD registry mode
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "42");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "42");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://any-invalid-url.com/");
        useReservedDatasetForId(42);
        validate();
    }

    @Test
    public void testValidate_ReservedStatusInDataset_NotGtldRegistryMode_FailsValidation() {
        // Reserved status does NOT exempt from IANA check when NOT in gTLD registry mode
        doReturn(false).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['handle']", "9994");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "9994");
        replaceValue("$['entities'][0]['links'][0]['value']", "https://invalid-iana-url.com/");
        useReservedDatasetForId(9994);
        validate(-47701,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://invalid-iana-url.com/\"}",
                "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_AccreditedRegistrar_GtldRegistryMode_StillValidatesIANA() {
        // An Accredited registrar (non-Reserved) is always validated against IANA,
        // even when gTLD registry mode is enabled.
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        replaceValue("$['entities'][0]['links'][0]['value']", "https://invalid-iana-url.com/");
        // Default mock returns an Accredited record (see RDAPDatasetServiceMock / RegistrarIdTest.getValidRecord())
        validate(-47701,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://invalid-iana-url.com/\"}",
                "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_HandleIsNull_AddResults47701() {
        removeKey("$['entities'][0]['handle']");
        validate(-47701,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_HandleIsNotNumber_AddResults47701() {
        replaceValue("$['entities'][0]['handle']", "invalid-handle");
        replaceValue("$['entities'][0]['publicIds'][0]['identifier']", "invalid-handle");
        validate(-47701,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://some-valid-uri.com/\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The registrar base URL is not registered with IANA.");
    }

    @Test
    public void testValidate_HrefIsNull_AddResults47703() {
        removeKey("$['entities'][0]['links'][0]['href']");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefHostStartsWithDot_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['href']", "https://.example.com/path");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://.example.com/path\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefHostEndsWithDot_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['href']", "https://example.com./path");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://example.com./path\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testValidate_HrefHostContainsSpaces_AddResults47703() {
        replaceValue("$['entities'][0]['links'][0]['href']", "https://exam ple.com/path");
        validate(-47703,
                "#/entities/0/links/0:{\"rel\":\"about\",\"href\":\"https://exam ple.com/path\",\"type\":\"text/html\",\"value\":\"https://example.com/\"}",
                "The 'href' property is not a valid Web URI according to [webUriValidation].");
    }

    @Test
    public void testDefensiveLines_NullScheme_UsingMockedCommonUtils() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        try (MockedStatic<CommonUtils> mockedCommonUtils = mockStatic(CommonUtils.class)) {
            URI mockUri = URI.create("https://example.com/test");

            mockedCommonUtils.when(() -> CommonUtils.createUri(anyString())).thenReturn(mockUri);
            mockedCommonUtils.when(() -> CommonUtils.getUriScheme(mockUri)).thenReturn(null);
            mockedCommonUtils.when(() -> CommonUtils.getUriHost(mockUri)).thenReturn("example.com");

            boolean result = validator.validHrefUri("https://example.com/test", "#/test-null-scheme");
            assertFalse(result, "Validation should fail when scheme is null");
        }
    }

    @Test
    public void testDefensiveLines_NullHost_UsingMockedCommonUtils() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();
        try (MockedStatic<CommonUtils> mockedCommonUtils = mockStatic(CommonUtils.class)) {
            URI mockUri = URI.create("https://example.com/test");

            mockedCommonUtils.when(() -> CommonUtils.createUri(anyString())).thenReturn(mockUri);
            mockedCommonUtils.when(() -> CommonUtils.getUriScheme(mockUri)).thenReturn("https");
            mockedCommonUtils.when(() -> CommonUtils.getUriHost(mockUri)).thenReturn(null);

            boolean result = validator.validHrefUri("https://example.com/test", "#/test-null-host");
            assertFalse(result, "Validation should fail when host is null");
        }
    }

    @Test
    public void testDefensiveLines_EmptyHost_UsingMockedCommonUtils() {
        ResponseValidation2Dot4Dot6_2024 validator = (ResponseValidation2Dot4Dot6_2024) getProfileValidation();

        try (MockedStatic<CommonUtils> mockedCommonUtils = mockStatic(CommonUtils.class)) {
            URI mockUri = URI.create("https://example.com/test");

            mockedCommonUtils.when(() -> CommonUtils.createUri(anyString())).thenReturn(mockUri);
            mockedCommonUtils.when(() -> CommonUtils.getUriScheme(mockUri)).thenReturn("https");
            mockedCommonUtils.when(() -> CommonUtils.getUriHost(mockUri)).thenReturn("   ");

            boolean result = validator.validHrefUri("https://example.com/test", "#/test-empty-host");
            assertFalse(result, "Validation should fail when host is empty/whitespace");
        }
    }
}