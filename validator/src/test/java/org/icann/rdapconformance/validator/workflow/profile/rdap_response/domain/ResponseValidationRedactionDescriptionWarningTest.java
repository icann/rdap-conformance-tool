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
     * Multiple redaction objects each with a distinct description → all corresponding
     * warnings (-65800, -65801, -65802, -65803, -65804) are emitted in a single validation run.
     */
    @Test
    public void testMultipleDescriptions_AllWarningsTriggered() {
        jsonObject.getJSONArray("redacted")
                .put(buildRedactionWithDescription("Registry Domain ID"))
                .put(buildRedactionWithDescription("Registry Registrant ID"))
                .put(buildRedactionWithDescription("Registrant Name"))
                .put(buildRedactionWithDescription("Registrant Organization"))
                .put(buildRedactionWithDescription("Registrant Street"))
                .put(buildRedactionWithDescription("Registrant City"));

        updateQueryContextJsonData();
        ProfileValidation validation = getProfileValidation();
        assertThat(validation.validate()).isFalse();

        ArgumentCaptor<RDAPValidationResult> captor =
                ArgumentCaptor.forClass(RDAPValidationResult.class);
        verify(results, times(6)).add(captor.capture());

        List<Integer> codes = captor.getAllValues().stream()
                .map(RDAPValidationResult::getCode)
                .toList();
        assertThat(codes).containsExactlyInAnyOrder(-65800, -65801, -65802, -65803, -65804, -65805);
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