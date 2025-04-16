package org.icann.rdapconformance.validator.workflow.rdap;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.icann.rdapconformance.validator.exception.parser.ExceptionParser.UNKNOWN_ERROR_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPValidationResultFileTest {

    private FileSystem fileSystem;
    private RDAPValidatorResults results;
    private ConfigurationFile configurationFile;

    @BeforeMethod
    public void setUp() {
        // Reset the singleton instance before each test
        RDAPValidationResultFile.reset();

        results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        fileSystem = mock(FileSystem.class);
        results.addGroups(Set.of("firstGroup"));
        configurationFile = mock(ConfigurationFile.class);

        // Initialize the singleton instance
        RDAPValidationResultFile.getInstance().initialize(
            results,
            mock(RDAPValidatorConfiguration.class),
            configurationFile,
            fileSystem
        );
    }

    @Test
    public void testGroupOkAssigned() throws IOException {
        RDAPValidationResultFile.getInstance().build(HTTP_OK);
        verify(fileSystem).write(any(), contains("\"groupOK\": [\"firstGroup\"]"));
    }

    @Test
    public void testGroupErrorWarningAssigned() throws IOException {
        results.addGroupErrorWarning("secondGroup");
        RDAPValidationResultFile.getInstance().build(HTTP_OK);
        verify(fileSystem).write(any(), contains("\"groupErrorWarning\": [\"secondGroup\"]"));
    }

    @Test
    public void testIgnore() throws IOException {
        int ignoredCode = -1000;
        results.add(RDAPValidationResult.builder()
                                        .code(ignoredCode)
                                        .value("ignoreCode")
                                        .message("this is a code to ignore")
                                        .build());
        results.add(RDAPValidationResult.builder()
                                        .code(UNKNOWN_ERROR_CODE)
                                        .value("unknown_code")
                                        .message("We log unknown error code, but they aren't part of the result file")
                                        .build());
        doReturn(List.of(ignoredCode)).when(configurationFile).getDefinitionIgnore();
        RDAPValidationResultFile.getInstance().build(HTTP_OK);
        // error should be an empty list since the only result code must be ignored:
        verify(fileSystem).write(any(), contains("\"error\": []"));
    }

    @Test
    public void testResultsFilePath() throws IOException {
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        String customResultsFilePath = "custom_results.json";
        doReturn(customResultsFilePath).when(config).getResultsFile();

        RDAPValidationResultFile.reset();
        RDAPValidationResultFile.getInstance().initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        RDAPValidationResultFile.getInstance().build(HTTP_OK);

        // Verify that the results are written to the custom file path
        verify(fileSystem).write(eq(customResultsFilePath), any(String.class));
    }

    @Test
    public void testDefaultResultsFilePath() throws IOException {
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        doReturn(null).when(config).getResultsFile();

        RDAPValidationResultFile.reset();
        RDAPValidationResultFile.getInstance().initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        RDAPValidationResultFile.getInstance().build(HTTP_OK);

        // Verify that the results are written to the default file path
        verify(fileSystem).mkdir("results");
        verify(fileSystem).write(contains("results/results-"), any(String.class));
    }
}