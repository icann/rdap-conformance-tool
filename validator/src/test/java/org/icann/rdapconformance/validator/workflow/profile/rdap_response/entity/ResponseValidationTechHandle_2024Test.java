package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import static org.mockito.Mockito.doReturn;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceTestMock;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Set;

public class ResponseValidationTechHandle_2024Test extends ProfileJsonValidationTestBase {

    static final String invalidTechHandleValue = "2138514test";

    public ResponseValidationTechHandle_2024Test() {
        super("/validators/profile/response_validations/entity/valid.json",
                "rdapResponseProfile_tech_handle_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        doReturn(true).when(queryContext.getConfig()).isGtldRegistry();
        doReturn(false).when(queryContext.getConfig()).isGtldRegistrar();
        doReturn(false).when(queryContext.getConfig()).isThin();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationTechHandle_2024(queryContext);
    }

    /**
     * Override base testValidate_ok() because valid.json has handle "123" for the technical
     * entity which does not match the RFC5730 pattern. We fix it here without changing valid.json.
     */
    @Test
    @Override
    public void testValidate_ok() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.put("handle", "TECH1-IANA");
        validate();
    }

    /**
     * Test -65700: technical entity handle does NOT comply with RFC5730 pattern.
     */
    @Test
    public void ResponseValidationTechHandle_2024_65700() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.put("handle", "2138514test"); // no dash → invalid
        validate(-65700, invalidTechHandleValue,
                "The handle of the technical entity does not comply with the format "
                        + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
    }

    /**
     * Test -65701: handle format is valid (passes RFC5730) but EPPROID is not registered.
     */
    @Test
    public void ResponseValidationTechHandle_2024_65701() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.put("handle", "TESTTECH-INVALID8"); // valid format, bad EPPROID

        // Custom dataset mock that marks "INVALID8" as an invalid EPPROID
        RDAPDatasetServiceTestMock customDatasets = new RDAPDatasetServiceTestMock(Set.of("INVALID8"));
        assertTrue(customDatasets.get(EPPRoid.class).isInvalid("INVALID8"));

        QueryContext testContext = new QueryContext(
                queryContext.getQueryId(),
                queryContext.getConfig(),
                customDatasets,
                queryContext.getQuery(),
                queryContext.getResults(),
                RDAPQueryType.DOMAIN
        );
        testContext.setRdapResponseData(jsonObject.toString());

        ResponseValidationTechHandle_2024 validator = new ResponseValidationTechHandle_2024(testContext);
        assertFalse(validator.doValidate());
    }

    /**
     * Valid case: handle matches RFC5730 and EPPROID is registered → no error.
     */
    @Test
    public void testValidTechHandle_passesValidation() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.put("handle", "TECH123-IANA");
        validate();
    }

    /**
     * No technical entity present → skip, no error.
     */
    @Test
    public void testNoTechnicalEntity_skipsValidation() {
        JSONArray roles = jsonObject.getJSONArray("entities").getJSONObject(1).getJSONArray("roles");
        roles.put(0, "registrar");
        validate();
    }

    /**
     * Technical entity present but no handle → skip, no error (handle is optional).
     */
    @Test
    public void testTechnicalEntityNoHandle_skipsValidation() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle");
        validate();
    }
}