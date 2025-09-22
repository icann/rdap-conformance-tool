package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidationTest;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONArray;
import org.json.JSONObject;
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

        doReturn(true).when(config).isGtldRegistry();
    }

    @Override
    protected String givenInvalidHandle() {
        // Change the existing registrar entity to have "billing" role and invalid handle
        replaceValue("$['entities'][0]['roles']", new JSONArray().put("billing"));
        replaceValue("$['entities'][0]['handle']", "ABCD"); 
        return "#/entities/0/handle:ABCD";
    }

    @Override
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
        doReturn(true).when(config).isGtldRegistry();
        doReturn(false).when(config).isThin();

        var validation = new ResponseValidation2Dot7Dot3_2024(config, rdapContent, results,
            mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService.class), RDAPQueryType.ENTITY);
        assertThat(validation.doLaunch()).isFalse();
    }

    @Test
    public void testDoLaunch_GtldRegistryThinRegistry_ReturnsFalse() {
        // Test that thin registries don't launch this validation
        doReturn(true).when(config).isGtldRegistry();
        doReturn(true).when(config).isThin();
        doReturn(false).when(config).isGtldRegistrar();

        var validation = new ResponseValidation2Dot7Dot3_2024(config, rdapContent, results,
            mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService.class), RDAPQueryType.DOMAIN);
        assertThat(validation.doLaunch()).isFalse();
    }

    @Test
    public void testDoLaunch_GtldRegistrar_ReturnsTrue() {
        // Test that gTLD registrars launch this validation
        doReturn(false).when(config).isGtldRegistry();
        doReturn(false).when(config).isThin();
        doReturn(true).when(config).isGtldRegistrar();

        var validation = new ResponseValidation2Dot7Dot3_2024(config, rdapContent, results,
            mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService.class), RDAPQueryType.DOMAIN);
        assertThat(validation.doLaunch()).isTrue();
    }

    @Test
    public void testDoLaunch_NonGtldRegistryAndNonGtldRegistrar_ReturnsFalse() {
        // Test that neither gTLD registry nor registrar doesn't launch
        doReturn(false).when(config).isGtldRegistry();
        doReturn(false).when(config).isThin();
        doReturn(false).when(config).isGtldRegistrar();

        var validation = new ResponseValidation2Dot7Dot3_2024(config, rdapContent, results,
            mock(org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService.class), RDAPQueryType.DOMAIN);
        assertThat(validation.doLaunch()).isFalse();
    }
}