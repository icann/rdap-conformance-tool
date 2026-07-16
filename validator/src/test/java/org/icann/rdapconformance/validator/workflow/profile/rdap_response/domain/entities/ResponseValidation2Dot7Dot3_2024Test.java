package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.lang.reflect.Field;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class ResponseValidation2Dot7Dot3_2024Test extends HandleValidationTest<ResponseValidation2Dot7Dot3_2024> {
    public ResponseValidation2Dot7Dot3_2024Test() {
        super("/validators/domain/valid.json", "rdapResponseProfile_2_7_3_validation",
            RDAPQueryType.DOMAIN, ResponseValidation2Dot7Dot3_2024.class, "entity");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();

        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        // Default off; tests that specifically exercise registrar mode override this
        // (see testValidate_RegistrarMode* below). Without this override the base
        // ProfileValidationTestBase leaves isGtldRegistrar()=true, which would send
        // every base HandleValidation test through the thick-registry lookup and
        // (after the getEntityHandle fix) skip validation, masking expected errors.
        doReturn(false).when(queryContext.getConfig()).isGtldRegistrar();
    }

    protected String givenInvalidHandle() {
        // Change the existing registrar entity to have "billing" role and invalid handle
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "ABCD");
        return "#/entities/0/handle:ABCD";
    }

    protected String getValidValueWithRoidExmp() {
        // Change the existing registrar entity to have "billing" role and valid handle  
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "2138514_DOMAIN_COM-EXMP");
        return "#/entities/0/handle:2138514_DOMAIN_COM-EXMP";
    }

    protected String givenNullHandle() {
        // Change the existing registrar entity to have "billing" role and null handle
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", null);
        return "#/entities/0/handle:null";
    }

    protected String givenNullHandle2() {
        // Change the existing registrar entity to have "billing" role and remove handle field
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        removeKey("$['entities'][0]['handle']");
        return "#/entities/0/handle:null";
    }

    @Test
    public void testValidate_ok() {
        getValidValueWithRoidExmp();

        super.testValidate_ok();
    }

    @Test
    public void testValidate_HandleIsNull_AddErrorCode() {
        String value = givenNullHandle();
        getProfileValidation();
        validate(-47600, value,
            "The handle in the entity object does not comply with the format "
                + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void testValidate_HandleIsNull2_AddErrorCode() {
        String value = givenNullHandle2();
        getProfileValidation();
        validate(-47600, value,
            "The handle in the entity object does not comply with the format "
                + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void testValidate_TopLevelRegistrarEntityExcluded_NoErrors() throws IOException {
        rdapContent = getResource("/validators/profile/rdap_response/domain/entities/hhgames_com_response.json");
        jsonObject = new JSONObject(rdapContent);
        getProfileValidation();
        super.testValidate_ok();
    }

    @Test
    public void testValidate_TopLevelResellerEntityExcluded_NoErrors() {
        // Change the existing registrar entity to have "reseller" role with invalid handle
        // This should NOT trigger -47600 because reseller entities are excluded
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("reseller"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");
        getProfileValidation();
        super.testValidate_ok();
    }

    @Test
    public void testValidate_TopLevelRegistrantEntityExcluded_NoErrors() {
        // Change the existing registrar entity to have "registrant" role with invalid handle
        // This should NOT trigger -47600 because registrant entities are excluded
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("registrant"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");
        getProfileValidation();
        super.testValidate_ok();
    }

    @Test
    public void testValidate_TopLevelTechnicalEntityExcluded_NoErrors() {
        // Change the existing registrar entity to have "technical" role with invalid handle
        // This should NOT trigger -47600 because technical entities are excluded
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("technical"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");
        getProfileValidation();
        super.testValidate_ok();
    }

    @Test
    public void testDoLaunch_NonDomainQuery_ReturnsFalse() {
        // Test that validation does not launch for non-DOMAIN queries
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(false).when(queryContext.getConfig()).isThin();

        QueryContext entityContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.ENTITY
        );
        entityContext.setRdapResponseData(queryContext.getRdapResponseData());
        var validation = new ResponseValidation2Dot7Dot3_2024(entityContext);
        assertThat(validation.doLaunch()).isFalse();
    }

    @Test
    public void testDoLaunch_GtldRegistryThinRegistry_ReturnsFalse() {
        // Test that thin registries don't launch this validation
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(true).when(queryContext.getConfig()).isThin();
        doReturn(false).when(queryContext.getConfig()).isGtldRegistrar();

        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        var validation = new ResponseValidation2Dot7Dot3_2024(domainContext);
        assertThat(validation.doLaunch()).isFalse();
    }

    @Test
    public void testDoLaunch_GtldRegistrar_ReturnsTrue() {
        // Test that gTLD registrars launch this validation
        doReturn(false).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(false).when(queryContext.getConfig()).isThin();
        doReturn(true).when(queryContext.getConfig()).isGtldRegistrar();

        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        var validation = new ResponseValidation2Dot7Dot3_2024(domainContext);
        assertThat(validation.doLaunch()).isTrue();
    }

    @Test
    public void testDoLaunch_NonGtldRegistryAndNonGtldRegistrar_ReturnsFalse() {
        // Test that neither gTLD registry nor registrar doesn't launch
        doReturn(false).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(false).when(queryContext.getConfig()).isThin();
        doReturn(false).when(queryContext.getConfig()).isGtldRegistrar();

        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        var validation = new ResponseValidation2Dot7Dot3_2024(domainContext);
        assertThat(validation.doLaunch()).isFalse();
    }

    @Test
    public void testValidate_RegistrarModeThickRegistry_RunsValidation() throws Exception {
        // In registrar mode, when the entity IS in the thick registry, validation runs
        // and errors (like -47600) are emitted.
        doReturn(false).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(true).when(queryContext.getConfig()).isGtldRegistrar();

        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");
        updateQueryContextJsonData();

        ResponseValidation2Dot7Dot3_2024 validation = (ResponseValidation2Dot7Dot3_2024) getProfileValidation();

        // Simulate thick registry: entity IS found -> validation must proceed.
        injectLookupServiceReturning(validation, true);

        ArgumentCaptor<RDAPValidationResult> resultCaptor =
                ArgumentCaptor.forClass(RDAPValidationResult.class);

        assertThat(validation.validate()).isFalse();
        Mockito.verify(results).add(resultCaptor.capture());
        RDAPValidationResult result = resultCaptor.getValue();
        assertThat(result).hasFieldOrPropertyWithValue("code", -47600)
                .hasFieldOrPropertyWithValue("value", "#/entities/0/handle:INVALID_HANDLE")
                .hasFieldOrPropertyWithValue("message",
                        "The handle in the entity object does not comply with the format "
                                + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
        org.mockito.Mockito.verify(results).addGroupErrorWarning(validation.getGroupName());
    }

    @Test
    public void testValidate_RegistrarModeThinRegistry_SkipsValidation() throws Exception {
        // In registrar mode, when the entity is NOT in the registry (thin registry
        // scenario), validation is intentionally skipped to avoid false positives —
        // the registrar holds the contact data, not the registry.
        doReturn(false).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(true).when(queryContext.getConfig()).isGtldRegistrar();

        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");
        updateQueryContextJsonData();

        ResponseValidation2Dot7Dot3_2024 validation = (ResponseValidation2Dot7Dot3_2024) getProfileValidation();

        // Simulate thin registry: entity NOT found -> validation must be skipped.
        injectLookupServiceReturning(validation, false);

        // Validation should pass (isValid=true) because the entity was skipped.
        assertThat(validation.validate()).isTrue();
        // And no -47600 error should have been recorded for that entity.
        org.mockito.Mockito.verify(results, org.mockito.Mockito.never())
                .add(org.mockito.ArgumentMatchers.any(RDAPValidationResult.class));
    }


    @Test
    public void testValidate_RegistryMode_AlwaysRunsValidation() {
        // Test registry mode - validation should always run regardless of thick/thin
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(false).when(queryContext.getConfig()).isThin();
        doReturn(false).when(queryContext.getConfig()).isGtldRegistrar();

        // Set up entity with invalid handle
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");

        getProfileValidation();
        validate(-47600, "#/entities/0/handle:INVALID_HANDLE",
            "The handle in the entity object does not comply with the format "
                + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void testValidate_RegistrarModeNoDomainName_StillValidates() {
        // When no domain name can be extracted (no ldhName, no unicodeName), the
        // thick-registry lookup cannot be performed, so the guard falls through
        // and validation runs normally.
        doReturn(false).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(true).when(queryContext.getConfig()).isGtldRegistrar();

        // Remove domain name identifiers so getDomainName() returns null.
        // Use JSONObject.remove (no-op if absent) to avoid Jayway PathNotFoundException.
        jsonObject.remove("ldhName");
        jsonObject.remove("unicodeName");
        rdapContent = jsonObject.toString();

        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");

        getProfileValidation();
        validate(-47600, "#/entities/0/handle:INVALID_HANDLE",
                "The handle in the entity object does not comply with the format "
                        + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void testValidate_EntityWithArrayIndex_ProperJsonPointerConversion() {
        // Test that JSON pointer conversion handles array indices correctly
        // This exercises the convertJsonPointerToJsonPath method with array indices
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(false).when(queryContext.getConfig()).isThin();

        // Set the first entity to have billing role (not excluded) and invalid handle
        // This will trigger validation and test the JSON pointer conversion:
        // #/entities/0/handle should convert to $.entities[0].handle (not $.entities.0.handle)
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "INVALID_HANDLE");

        getProfileValidation();

        // The validation should find the error and use proper array notation in JSON pointer conversion
        validate(-47600, "#/entities/0/handle:INVALID_HANDLE",
            "The handle in the entity object does not comply with the format "
                + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    @Test
    public void testValidate_MultipleTopLevelEntities_RegistrantExcluded_NoFalsePositive()
            throws IOException {
        // Regression: registrant entity was previously included by the exclusion
        // filter due to Jayway '=~' + roles[*] misbehavior, producing -47600 when
        // its handle was redacted (missing). See RCT-548 (-47600).
        rdapContent = getResource(
                "/validators/profile/rdap_response/domain/entities/nomeo_multi_entity_response.json");
        jsonObject = new JSONObject(rdapContent);
        getProfileValidation();
        super.testValidate_ok();
    }

    @Test
    public void testGetEntityHandle_ResolvesJsonPointerCorrectly() throws Exception {
        // Regression: getEntityHandle used to convert the JSON Pointer
        // "#/entities/0/handle" to a JSONPath "$.entities[0].handle" and pass it
        // to JSONObject.query(...), which is RFC 6901 JSONPointer-based. The
        // JSONPath string does not start with '/' or '#', so JSONPointer threw
        // IllegalArgumentException, the catch swallowed it, and the method
        // always returned null — silently bypassing the thick-registry lookup
        // in registrar mode.
        replaceValue("$['entities'][0]['handle']", "SOME-REAL-HANDLE");

        ResponseValidation2Dot7Dot3_2024 validation = (ResponseValidation2Dot7Dot3_2024) getProfileValidation();

        java.lang.reflect.Method m =
                ResponseValidation2Dot7Dot3_2024.class.getDeclaredMethod(
                        "getEntityHandle", String.class);
        m.setAccessible(true);

        Object result = m.invoke(validation, "#/entities/0/handle");

        assertThat(result)
                .as("getEntityHandle must resolve the JSON Pointer, not return null")
                .isEqualTo("SOME-REAL-HANDLE");
    }

    /**
     * Injects a mocked EntityRegistryLookupService into the validation instance
     * so tests can control the thick/thin registry outcome without real HTTP calls.
     */
    private static void injectLookupServiceReturning(
            ResponseValidation2Dot7Dot3_2024 validation, boolean entityFound) throws Exception {
        EntityRegistryLookupService mockService = mock(EntityRegistryLookupService.class);
        doReturn(entityFound).when(mockService).isEntityInThickRegistry(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());

        Field serviceField =
                ResponseValidation2Dot7Dot3_2024.class.getDeclaredField("entityLookupService");
        serviceField.setAccessible(true);
        serviceField.set(validation, mockService);
    }
}