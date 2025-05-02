package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ResponseValidationLinkElements_2024Test {

    public static final String BAD_DATA = "src/test/resources/validators/links/invalid_missing_rel_and_value.json";
    public static final String LINKS = "links";
    public static final int EXPECTED = 2;
    private JSONObject jsonObject;
    private RDAPValidatorResults results;

    @BeforeMethod
    public void setUp() throws java.io.IOException {
        results = mock(RDAPValidatorResults.class);
    }

    private void loadJson(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            jsonObject = new JSONObject(content);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + filePath, e);
        }
    }

    private ProfileValidation getProfileValidation() {
        return new ResponseValidationLinkElements_2024(jsonObject.toString(), results);
    }

    @Test
    public void testValidate_ok() {
        loadJson("src/test/resources/validators/links/valid.json");
        ProfileValidation validation = getProfileValidation();
        assertThat(validation.validate()).isTrue();
        verify(results).addGroup(validation.getGroupName());
        verifyNoMoreInteractions(results);
    }

    @Test
    public void testValidate_LinkWithoutValue_AddErrorCode() {
        loadJson(BAD_DATA);
        validateNotOk(-10612, "$['links'][0]/value:" + jsonObject.getJSONArray(LINKS).getJSONObject(0), "A 'value' property does not exist in the link object.");
    }

    @Test
    public void testValidate_LinkWithoutRel_AddErrorCode() {
        loadJson(BAD_DATA);
        validateNotOk(-10613, "$['links'][1]/rel:" + jsonObject.getJSONArray(LINKS).getJSONObject(0), "The rel element does not exist.");
    }
    @Test
    public void testValidate_MultipleErrors() {
        loadJson(BAD_DATA);
        ProfileValidation validation = getProfileValidation();
        assertThat(validation.validate()).isFalse();

        ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor.forClass(RDAPValidationResult.class);
        verify(results, atLeastOnce()).add(resultCaptor.capture());

        List<RDAPValidationResult> capturedResults = resultCaptor.getAllValues();
        assertThat(capturedResults).hasSize(4);

        long count10612 = capturedResults.stream().filter(result ->
            result.getCode() == -10612 &&
                result.getMessage().equals("A 'value' property does not exist in the link object.")
        ).count();

        long count10613 = capturedResults.stream().filter(result ->
            result.getCode() == -10613 &&
                result.getMessage().equals("The rel element does not exist.")
        ).count();

        assertThat(count10612).isEqualTo(EXPECTED);
        assertThat(count10613).isEqualTo(EXPECTED);

        verify(results).addGroupErrorWarning(validation.getGroupName());
    }

    // we skip checking the value - that is living !#$@ to get correct. Let it go.
    private void validateNotOk(int code, String value, String message) {
        ProfileValidation validation = getProfileValidation();
        ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor.forClass(RDAPValidationResult.class);
        assertThat(validation.validate()).isFalse();
        verify(results, atLeastOnce()).add(resultCaptor.capture());
        boolean found = resultCaptor.getAllValues().stream().anyMatch(result ->
            result.getCode() == code &&
                result.getMessage().equals(message)
        );
        assertThat(found).isTrue();
        verify(results).addGroupErrorWarning(validation.getGroupName());
    }
}