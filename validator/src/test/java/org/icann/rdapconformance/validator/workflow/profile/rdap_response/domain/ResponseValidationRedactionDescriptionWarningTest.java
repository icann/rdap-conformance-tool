package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;

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

    /**
     * Test -65801: redaction object with name.description = "Registry Registrant ID" → warning emitted.
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
     * Redaction with "Registry Registrant ID" → only -65801 fires, not -65800.
     * (Covered implicitly by test65801_RegistryRegistrantIdDescription_ShouldTrigger
     *  since validateNotOk verifies exactly one result.add() call)
     * This test explicitly confirms -65800 does NOT fire by using validateNotOk
     * with the correct -65801 code — if -65800 also fired, Mockito would throw.
     */
    @Test
    public void test65801_ShouldNotTrigger65800() {
        JSONObject redacted = buildRedactionWithDescription("Registry Registrant ID");
        jsonObject.getJSONArray("redacted").put(redacted);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = "#/redacted/" + insertedIndex + ":" +
                jsonObject.getJSONArray("redacted").getJSONObject(insertedIndex).toString();

        validate(-65801, expectedValue,
                "A redaction object with a description of Registry Registrant ID exists. " +
                        "This warning may be ignored if the redaction should not use the 'type' property.");
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