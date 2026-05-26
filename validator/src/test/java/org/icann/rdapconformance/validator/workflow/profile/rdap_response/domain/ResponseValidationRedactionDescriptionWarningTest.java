package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.mockito.ArgumentCaptor;

import java.util.List;

public class ResponseValidationRedactionDescriptionWarningTest extends ProfileJsonValidationTestBase {

    public ResponseValidationRedactionDescriptionWarningTest() {
        super("/validators/profile/response_validations/entity/valid.json",
                "rdapResponseProfile_redaction_description_warning_Validation");
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
        return new ResponseValidationRedactionDescriptionWarning(queryContext);
    }

    /**
     * No redaction object with description "Registry Domain ID" → no warning, passes.
     */
    @Test
    @Override
    public void testValidate_ok() {
        // valid.json redacted array has no entry with description "Registry Domain ID"
        validate();
    }

    /**
     * Test -65800: redaction object with name.description = "Registry Domain ID" → warning emitted.
     */
    @Test
    public void test65800_RegistryDomainIdDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registry Domain ID");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65800, expectedValue,
                "A redaction object with a description of Registry Domain ID exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Redaction present but name.description is a different value → no -65800.
     */
    @Test
    public void test65800_OtherDescription_ShouldNotTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Some Other Field");
        jsonObject.getJSONArray("redacted").put(redacted);
        validate();
    }

    /**
     * Redaction present but no name object → no -65800.
     */
    @Test
    public void test65800_NoNameObject_ShouldNotTrigger() {
        JSONObject redacted = new JSONObject();
        redacted.put("method", "removal");
        jsonObject.getJSONArray("redacted").put(redacted);
        validate();
    }

    /**
     * Redaction present, name object exists but no description property → no -65800.
     */
    @Test
    public void test65800_NoDescriptionProperty_ShouldNotTrigger() {
        JSONObject redacted = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Domain ID"); // only type, no description
        redacted.put("name", name);
        jsonObject.getJSONArray("redacted").put(redacted);
        validate();
    }

    /** * Test -65801: redaction object with name.description = "Registry Registrant ID" → warning emitted.
     *  Note: validateNotOk verifies exactly one results.add() call via Mockito,
     *  so this test also implicitly guarantees that -65800 does NOT fire.
     */
    @Test
    public void test65801_RegistryRegistrantIdDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registry Registrant ID");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65801, expectedValue,
                "A redaction object with a description of Registry Registrant ID exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65802: redaction object with name.description = "Registrant Name" → warning emitted.
     * Note: validateNotOk verifies exactly one results.add() call via Mockito,
     * so this test also implicitly guarantees that -65800 and -65801 do NOT fire.
     */
    @Test
    public void test65802_RegistrantNameDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Name");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65802, expectedValue,
                "A redaction object with a description of Registrant Name exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65803: redaction object with name.description = "Registrant Organization" → warning emitted.
     * Note: validateNotOk verifies exactly one results.add() call via Mockito,
     * so this test also implicitly guarantees that no other warning code fires.
     */
    @Test
    public void test65803_RegistrantOrganizationDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Organization");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65803, expectedValue,
                "A redaction object with a description of Registrant Organization exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65804: redaction object with name.description = "Registrant Street" → warning emitted.
     * Note: validateNotOk verifies exactly one results.add() call via Mockito,
     * so this test also implicitly guarantees that no other warning code fires.
     */
    @Test
    public void test65804_RegistrantStreetDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Street");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65804, expectedValue,
                "A redaction object with a description of Registrant Street exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /** * Test -65805: redaction object with name.description = "Registrant City" → warning emitted. * Note: validateNotOk verifies exactly one results.add() call via Mockito, * so this test also implicitly guarantees that no other warning code fires. */
    @Test
    public void test65805_RegistrantCityDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant City");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65805, expectedValue,
                "A redaction object with a description of Registrant City exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65806: redaction object with name.description = "Registrant Postal Code" → warning emitted.
     */
    @Test
    public void test65806_RegistrantPostalCodeDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Postal Code");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65806, expectedValue,
                "A redaction object with a description of Registrant Postal Code exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65807: redaction object with name.description = "Registrant Phone" → warning emitted.
     */
    @Test
    public void test65807_RegistrantPhoneDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Phone");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65807, expectedValue,
                "A redaction object with a description of Registrant Phone exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65808: redaction object with name.description = "Registrant Phone Ext" → warning emitted.
     */
    @Test
    public void test65808_RegistrantPhoneExtDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Phone Ext");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65808, expectedValue,
                "A redaction object with a description of Registrant Phone Ext exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65809: redaction object with name.description = "Registrant Fax" → warning emitted.
     */
    @Test
    public void test65809_RegistrantFaxDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Fax");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65809, expectedValue,
                "A redaction object with a description of Registrant Fax exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65810: redaction object with name.description = "Registrant Fax Ext" → warning emitted.
     */
    @Test
    public void test65810_RegistrantFaxExtDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Fax Ext");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65810, expectedValue,
                "A redaction object with a description of Registrant Fax Ext exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65811: redaction object with name.description = "Registrant Email" → warning emitted.
     */
    @Test
    public void test65811_RegistrantEmailDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registrant Email");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65811, expectedValue,
                "A redaction object with a description of Registrant Email exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /** * Test -65812: redaction object with name.description = "Registry Tech ID"
     * → warning emitted.
     */
    @Test
    public void test65812_RegistryTechIDDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Registry Tech ID");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65812, expectedValue,
                "A redaction object with a description of Registry Tech ID exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65813: redaction object with name.description = "Tech Name" → warning emitted.
     */
    @Test
    public void test65813_TechNameDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Tech Name");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65813, expectedValue,
                "A redaction object with a description of Tech Name exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /** * Test -65814: redaction object with name.description = "Tech Phone" → warning emitted. */
    @Test
    public void test65814_TechPhoneDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Tech Phone");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65814, expectedValue,
                "A redaction object with a description of Tech Phone exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65815: redaction object with name.description = "Tech Phone Ext" → warning emitted.
     */
    @Test
    public void test65815_TechPhoneExtDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Tech Phone Ext");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65815, expectedValue,
                "A redaction object with a description of Tech Phone Ext exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Test -65816: redaction object with name.description = "Tech Email" → warning emitted.
     */
    @Test
    public void test65816_TechEmailDescription_ShouldTrigger() {
        JSONObject redacted = buildRedactionWithDescription("Tech Email");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65816, expectedValue,
                "A redaction object with a description of Tech Email exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
    }

    /**
     * Multiple redaction objects each with a distinct description → all corresponding
     * warnings (-65800, -65801, -65802, -65803, -65804, -65805, -65806, -65807, -65808, -65809, -65810, -65811, -65812, -65813, -65814, -65815, -65816)
     * are emitted in a single validation run.
     */
    @Test
    public void testMultipleDescriptions_AllWarningsTriggered() {
        jsonObject.getJSONArray("redacted")
                .put(buildRedactionWithDescription("Registry Domain ID"))
                .put(buildRedactionWithDescription("Registry Registrant ID"))
                .put(buildRedactionWithDescription("Registrant Name"))
                .put(buildRedactionWithDescription("Registrant Organization"))
                .put(buildRedactionWithDescription("Registrant Street"))
                .put(buildRedactionWithDescription("Registrant City"))
                .put(buildRedactionWithDescription("Registrant Postal Code"))
                .put(buildRedactionWithDescription("Registrant Phone"))
                .put(buildRedactionWithDescription("Registrant Phone Ext"))
                .put(buildRedactionWithDescription("Registrant Fax"))
                .put(buildRedactionWithDescription("Registrant Fax Ext"))
                .put(buildRedactionWithDescription("Registrant Email"))
                .put(buildRedactionWithDescription("Registry Tech ID"))
                .put(buildRedactionWithDescription("Tech Name"))
                .put(buildRedactionWithDescription("Tech Phone"))
                .put(buildRedactionWithDescription("Tech Phone Ext"))
                .put(buildRedactionWithDescription("Tech Email"));

        updateQueryContextJsonData();
        ProfileValidation validation = getProfileValidation();
        assertThat(validation.validate()).isFalse();

        ArgumentCaptor<RDAPValidationResult> captor =
                ArgumentCaptor.forClass(RDAPValidationResult.class);
        verify(results, times(17)).add(captor.capture());

        List<Integer> codes = captor.getAllValues().stream()
                .map(RDAPValidationResult::getCode)
                .toList();
        assertThat(codes).containsExactlyInAnyOrder(
                -65800, -65801, -65802, -65803, -65804, -65805,
                -65806, -65807, -65808, -65809, -65810, -65811, -65812,
                -65813, -65814, -65815, -65816);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private JSONObject buildRedactionWithDescription(String description) {
        JSONObject redacted = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("description", description);
        redacted.put("name", name);
        redacted.put("reason", new JSONObject().put("description", "Server policy"));
        return redacted;
    }
}