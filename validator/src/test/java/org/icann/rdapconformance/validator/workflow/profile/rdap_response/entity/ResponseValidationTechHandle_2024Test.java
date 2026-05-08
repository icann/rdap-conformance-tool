package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceTestMock;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
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
        techEntity.put("handle", "2138514test"); // no dash, does not match RFC5730 pattern
        validate(-65700, invalidTechHandleValue,
                "The handle of the technical entity does not comply with the format "
                        + "(\\w|_){1,80}-\\w{1,8} specified in RFC5730.");
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
     * Test -65701: handle format is valid (passes RFC5730) but EPPROID is not registered.
     */
    @Test
    public void ResponseValidationTechHandle_2024_65701() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.put("handle", "TESTTECH-INVALID8"); // valid RFC5730 format, unregistered EPPROID

        // Custom dataset mock that marks "INVALID8" as an invalid EPPROID
        RDAPDatasetServiceTestMock customDatasets = new RDAPDatasetServiceTestMock(Set.of("INVALID8"));

        // Build a QueryContext with the custom dataset but sharing the same results mock,
        // so that validateNotOk() can capture and assert the exact -65701 result produced.
        QueryContext testContext = new QueryContext(
                queryContext.getQueryId(),
                queryContext.getConfig(),
                customDatasets,
                queryContext.getQuery(),
                queryContext.getResults(),
                RDAPQueryType.DOMAIN
        );
        testContext.setRdapResponseData(jsonObject.toString());

        // Override getProfileValidation() result inline via the validate helper
        // by temporarily replacing the queryContext used by the base class.
        ArgumentCaptor<RDAPValidationResult> captor = ArgumentCaptor.forClass(RDAPValidationResult.class);
        ResponseValidationTechHandle_2024 validator = new ResponseValidationTechHandle_2024(testContext);

        assertFalse(validator.validate());
        verify(results).add(captor.capture());
        RDAPValidationResult result = captor.getValue();
        assertThat(result)
                .hasFieldOrPropertyWithValue("code", -65701)
                .hasFieldOrPropertyWithValue("value", "TESTTECH-INVALID8")
                .hasFieldOrPropertyWithValue("message",
                        "The globally unique identifier in the technical entity handle is not registered in EPPROID.");
        verify(results).addGroupErrorWarning(validator.getGroupName());
    }

    /**
     * Test -65702: handle is present AND a "Registry Tech ID" redaction exists — contradiction.
     */
    @Test
    public void testHandlePresentAndRedactedRegistryTechIdPresent_ShouldTrigger65702() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.put("handle", "TECH1-IANA"); // valid handle, present

        // Add a "Registry Tech ID" redaction entry to the redacted array
        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        validate(-65702, getResultValueFromRedactedPointers(),
                "a redaction of type Registry Tech ID was found but the technical handle was not redacted.");
    }

    /**
     * Handle present, no "Registry Tech ID" redaction → no -65702.
     */
    @Test
    public void testHandlePresentAndNoRedactedRegistryTechId_ShouldNotTrigger65702() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.put("handle", "TECH1-IANA");
        // redacted array in valid.json has no "Registry Tech ID" entry
        validate();
    }

    /**
     * Test -65703: no handle present AND no "Registry Tech ID" redaction → error required.
     */
    @Test
    public void ResponseValidationTechHandle_2024_65703() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle"); // no handle present

        // valid.json redacted array has no "Registry Tech ID" entry
        validate(-65703, getResultValueFromRedactedPointers(),
                "a redaction of type Registry Tech ID is required.");
    }

    /**
     * No handle present but "Registry Tech ID" redaction exists → no -65703, passes.
     */
    @Test
    public void testNoHandleWithRegistryTechIdRedaction_skips65703() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle"); // no handle

        // Add the required "Registry Tech ID" redaction
        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        validate();
    }

    /**
     * Test -65704: no handle, redaction present with pathLang "jsonpath" and invalid prePath.
     */
    @Test
    public void ResponseValidationTechHandle_2024_65704() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle");

        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("pathLang", "jsonpath");
        redactedTechId.put("prePath", "$.[");  // definitively invalid per JpathUtilTest
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = jsonObject.getJSONArray("redacted")
                .getJSONObject(insertedIndex)
                .toString();

        validate(-65704, expectedValue,
                "jsonpath is invalid for Registry Tech ID");
    }

    /**
     * No handle, redaction present with pathLang "jsonpath" and valid prePath → no error.
     */
    @Test
    public void testNoHandleRedactionWithValidPrePath_passes() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle");

        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("pathLang", "jsonpath");
        redactedTechId.put("prePath", "$.entities[?(@.roles[0]=='technical')].handle"); // valid JSONPath
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        validate();
    }

    /**
     * No handle, redaction present with pathLang absent and invalid prePath → -65704.
     */
    @Test
    public void testNoHandleRedactionNoPathLangInvalidPrePath_triggers65704() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle");

        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("prePath", "$.[");  // definitively invalid per JpathUtilTest
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = jsonObject.getJSONArray("redacted")
                .getJSONObject(insertedIndex)
                .toString();

        validate(-65704, expectedValue,
                "jsonpath is invalid for Registry Tech ID");
    }

    /**
     * No handle, redaction present, prePath absent → no -65704 (prePath is optional).
     */
    @Test
    public void testNoHandleRedactionNoPrePath_passes() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle");

        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("pathLang", "jsonpath");
        // no prePath — optional, should pass
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        validate();
    }

    /**
     * Test -65705: no handle, redaction present, prePath is valid JSONPath but evaluates
     * to a non-empty set (field still exists in the response) -> -65705.
     */
    @Test
    public void ResponseValidationTechHandle_2024_65705() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle"); // no handle

        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("pathLang", "jsonpath");
        // This JSONPath evaluates to a non-empty set because "objectClassName" exists in entities[1]
        redactedTechId.put("prePath", "$.entities[1].objectClassName");
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = jsonObject.getJSONArray("redacted")
                .getJSONObject(insertedIndex)
                .toString();

        validate(-65705, expectedValue,
                "jsonpath must evaluate to a zero set for redaction removal of Registry Tech ID.");
    }

    /**
     * No handle, redaction present, valid prePath evaluates to empty set -> no -65705.
     */
    @Test
    public void testNoHandleRedactionPrePathEmptySet_passes() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle"); // no handle — field is absent, path evaluates to empty set

        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        redactedTechId.put("pathLang", "jsonpath");
        // handle was removed above, so this path evaluates to empty set
        redactedTechId.put("prePath", "$.entities[?(@.roles[0]=='technical')].handle");
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        validate();
    }

    /**
     * No handle, redaction present, pathLang absent, valid prePath evaluates to non-empty set -> -65705.
     */
    @Test
    public void testNoHandleRedactionNoPathLangPrePathNonEmpty_triggers65705() {
        JSONObject techEntity = jsonObject.getJSONArray("entities").getJSONObject(1);
        techEntity.remove("handle");

        JSONObject redactedTechId = new JSONObject();
        JSONObject name = new JSONObject();
        name.put("type", "Registry Tech ID");
        redactedTechId.put("name", name);
        redactedTechId.put("method", "removal");
        // no pathLang — spec says absent pathLang also triggers prePath validation
        redactedTechId.put("prePath", "$.entities[1].objectClassName"); // non-empty set
        redactedTechId.put("reason", new JSONObject().put("description", "Server policy"));
        jsonObject.getJSONArray("redacted").put(redactedTechId);

        int insertedIndex = jsonObject.getJSONArray("redacted").length() - 1;
        String expectedValue = jsonObject.getJSONArray("redacted")
                .getJSONObject(insertedIndex)
                .toString();

        validate(-65705, expectedValue,
                "jsonpath must evaluate to a zero set for redaction removal of Registry Tech ID.");
    }

    // Helper — mirrors the one in ResponseValidationRegistrantHandle_2024Test
    private String getResultValueFromRedactedPointers() {
        JSONArray redacted = jsonObject.getJSONArray("redacted");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < redacted.length(); i++) {
            if (i > 0) sb.append(", ");
            sb.append("#/redacted/").append(i).append(":").append(redacted.getJSONObject(i).toString());
        }
        return sb.toString();
    }
}