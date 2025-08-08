package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.doReturn;

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
}