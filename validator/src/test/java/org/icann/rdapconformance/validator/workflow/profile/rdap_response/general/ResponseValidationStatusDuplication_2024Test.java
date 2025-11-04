package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ResponseValidationStatusDuplication_2024Test {

    private JSONObject jsonObject;
    private RDAPValidatorResults results;
    private QueryContext queryContext;

    @BeforeMethod
    public void setUp() throws java.io.IOException {
        results = mock(RDAPValidatorResults.class);
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        RDAPDatasetServiceMock datasets = new RDAPDatasetServiceMock();
        queryContext = QueryContext.forTesting("{}", results, config, datasets);
    }

    private void loadJson(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            jsonObject = new JSONObject(content);
            queryContext.setRdapResponseData(content);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to load JSON file: " + filePath, e);
        }
    }

    private ProfileValidation getProfileValidation() {
        return new ResponseValidationStatusDuplication_2024(queryContext);
    }

    @Test
    public void testValidate_ok() {
        loadJson("src/test/resources/validators/status/valid.json");
        ProfileValidation validation = getProfileValidation();
        assertThat(validation.validate()).isTrue();
        verify(results).addGroup(validation.getGroupName());
        verifyNoMoreInteractions(results);
    }

    @Test
    public void testValidate_DuplicateStatus_AddErrorCode() {
        loadJson("src/test/resources/validators/status/invalid_duplicate_status.json");
        ProfileValidation validation = getProfileValidation();
        assertThat(validation.validate()).isFalse();

        ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor.forClass(RDAPValidationResult.class);
        verify(results, atLeastOnce()).add(resultCaptor.capture());

        List<RDAPValidationResult> capturedResults = resultCaptor.getAllValues();
        assertThat(capturedResults).hasSize(2); // One for "server transfer prohibited" and one for "client delete prohibited"

        // Verify that the results contain the expected error messages, regardless of order
        assertThat(capturedResults).anySatisfy(result -> {
            assertThat(result.getCode()).isEqualTo(-11003);
            assertThat(result.getValue()).contains("server transfer prohibited");
            assertThat(result.getMessage()).isEqualTo("A status value exists more than once in the status array");
        });

        assertThat(capturedResults).anySatisfy(result -> {
            assertThat(result.getCode()).isEqualTo(-11003);
            assertThat(result.getValue()).contains("client delete prohibited");
            assertThat(result.getMessage()).isEqualTo("A status value exists more than once in the status array");
        });

        verify(results).addGroupErrorWarning(validation.getGroupName());
    }
}