package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.icann.rdapconformance.validator.exception.parser.ExceptionParser.UNKNOWN_ERROR_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.icann.rdapconformance.validator.BuildInfo;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPValidationResultFileTest {

    private FileSystem fileSystem;
    private RDAPValidatorResults results;
    private ConfigurationFile configurationFile;
  private RDAPValidationResultFile file;

    @BeforeMethod
    public void setUp() {
        RDAPValidationResultFile.reset();

        results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        fileSystem = mock(FileSystem.class);
        results.addGroups(Set.of("firstGroup"));
        configurationFile = mock(ConfigurationFile.class);

        RDAPValidationResultFile.getInstance().initialize(
            results,
            mock(RDAPValidatorConfiguration.class),
            configurationFile,
            fileSystem
        );

        file = RDAPValidationResultFile.getInstance();
    }

    @AfterMethod
    public void tearDown() {
        results.clear();
    }


    @Test
    public void testGroupOkAssigned() throws IOException {
        RDAPValidationResultFile.getInstance().build();
        verify(fileSystem).write(any(), contains("\"groupOK\": [\"firstGroup\"]"));
    }

    @Test
    public void testGroupErrorWarningAssigned() throws IOException {
        results.addGroupErrorWarning("secondGroup");
        RDAPValidationResultFile.getInstance().build();
        verify(fileSystem).write(any(), contains("\"groupErrorWarning\": [\"secondGroup\"]"));
    }

  @Test
  public void testGtldRegistrar() throws IOException {
    file.build();
    verify(fileSystem).write(any(), contains("\"gtldRegistrar\": false"));
  }
  @Test
  public void testGtldRegistry() throws IOException {
    file.build();
    verify(fileSystem).write(any(), contains("\"gtldRegistry\": false"));
  }

  @Test
  public void testThinRegistry() throws IOException {
    file.build();
    verify(fileSystem).write(any(), contains("\"thinRegistry\": false"));
  }

    @Test
    public void testConformanceToolVersion() throws IOException {
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        doReturn(true).when(config).useRdapProfileFeb2024();

        RDAPValidationResultFile.reset();
        RDAPValidationResultFile.getInstance().initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        RDAPValidationResultFile.getInstance().build();

        verify(fileSystem).write(any(), contains("\"conformanceToolVersion\": \"" + BuildInfo.getVersion() + "\""));
    }

    @Test
    public void testBuildDate() throws IOException {
        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        doReturn(true).when(config).useRdapProfileFeb2024();

        RDAPValidationResultFile.reset();
        RDAPValidationResultFile.getInstance().initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        RDAPValidationResultFile.getInstance().build();

        verify(fileSystem).write(any(), contains("\"buildDate\": \"" + BuildInfo.getBuildDate() + "\""));
    }



  @Test
  public void testProfileFebruary2019() throws IOException {
    file.build();
    verify(fileSystem).write(any(), contains("\"rdapProfileFebruary2019\": false"));
  }

  @Test
  public void testProfileFebruary2024() throws IOException {
    file.build();
    verify(fileSystem).write(any(), contains("\"rdapProfileFebruary2024\": false"));
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
    file.build();
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
        RDAPValidationResultFile.getInstance().build();

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
        RDAPValidationResultFile.getInstance().build();

    // Verify that the results are written to the default file path
    verify(fileSystem).mkdir("results");
    verify(fileSystem).write(contains("results/results-"), any(String.class));
  }

    @Test
    public void testAllIgnoredCodes() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        results.add(RDAPValidationResult.builder().code(-130004).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(-130005).httpStatusCode(404).build());

        String output = results.analyzeResultsWithStatusCheck();

        assertTrue(output.isEmpty());
        assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testAllNonIgnoredCodesSameStatus() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(200).build());

        String output = results.analyzeResultsWithStatusCheck();

        assertTrue(output.contains("code=1001, httpStatusCode=200"));
        assertTrue(output.contains("code=1002, httpStatusCode=200"));
        assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testAllNonIgnoredCodesDifferentStatus() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(404).build());

        String output = results.analyzeResultsWithStatusCheck();

        assertTrue(output.contains("code=1001, httpStatusCode=200"));
        assertTrue(output.contains("code=1002, httpStatusCode=404"));
        assertTrue(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
        RDAPValidationResult tupleResult = results.getAll().stream()
                                                  .filter(r -> r.getCode() == -13018)
                                                  .findFirst().orElse(null);
        assertNotNull(tupleResult);
        assertTrue(tupleResult.getValue().contains("[[1001,200],[1002,404]]") ||
            tupleResult.getValue().contains("[[1002,404],[1001,200]]"));
    }

    @Test
    public void testMixedIgnoredAndNonIgnoredCodes() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        results.add(RDAPValidationResult.builder().code(-130004).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(200).build());

        String output = results.analyzeResultsWithStatusCheck();

        assertFalse(output.contains("code=-130004"));
        assertTrue(output.contains("code=1001, httpStatusCode=200"));
        assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testNonIgnoredCodeWithZeroStatus() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(0).build());
        results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(200).build());

        results.analyzeResultsWithStatusCheck();

        RDAPValidationResult tupleResult = results.getAll().stream()
                                                  .filter(r -> r.getCode() == -13018)
                                                  .findFirst().orElse(null);
        assertNotNull(tupleResult);
        assertTrue(tupleResult.getValue().contains("[[1001,null],[1002,200]]") ||
            tupleResult.getValue().contains("[[1002,200],[1001,null]]"));
    }

    @Test
    public void testEmptyResults() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();

        String output = results.analyzeResultsWithStatusCheck();
        assertTrue(output.isEmpty());
        assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testNullAndZeroStatusCodesAreEquivalent() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();

        // add them going in different -- but the statusCodeFromCurrent will never let it be null. It will zero it out
        results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(null).build());
        results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(0).build());

        String output = results.analyzeResultsWithStatusCheck();

        // Both results should be in output
        assertTrue(output.contains("code=1001, httpStatusCode=0"));
        assertTrue(output.contains("code=1002, httpStatusCode=0"));
        // Should not generate -13018 error since we have same status codes (0 vs 0)
        assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testMixedNullZeroAndOtherStatusCodes() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();
        results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(null).build());
        results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(0).build());
        results.add(RDAPValidationResult.builder().code(1003).httpStatusCode(200).build());

        results.analyzeResultsWithStatusCheck();

        // Should generate -13018 error since we have different status codes (0/null vs 200)
        RDAPValidationResult tupleResult = results.getAll().stream()
                                                  .filter(r -> r.getCode() == -13018)
                                                  .findFirst().orElse(null);

        assertNotNull(tupleResult);
        assertTrue(tupleResult.getValue().contains("[1002,null],[1001,null],[1003,200]]") );
    }

    @Test
    public void testNoDuplicateTuplesInJson() {
        RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
        results.clear();

        // Add duplicate results deliberately
        results.add(RDAPValidationResult.builder().code(-52106).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(-52106).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(-13007).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(-13007).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(-61101).httpStatusCode(null).build());
        results.add(RDAPValidationResult.builder().code(-61101).httpStatusCode(null).build());
        results.add(RDAPValidationResult.builder().code(-23101).httpStatusCode(null).build());
        results.add(RDAPValidationResult.builder().code(-23101).httpStatusCode(null).build());

        results.analyzeResultsWithStatusCheck();

        // Grab the -13018 result which contains our tupleListJson
        RDAPValidationResult tupleResult = results.getAll().stream()
                                                  .filter(r -> r.getCode() == -13018)
                                                  .findFirst()
                                                  .orElse(null);

        assertNotNull(tupleResult);
        String tupleListJson = tupleResult.getValue();

        // Count occurrences of each code and ensure it's only there ONCE
        assertTrue(countOccurrences(tupleListJson, "[-52106,200]") == ONE,
            "[-52106,200] appears multiple times");
        assertTrue(countOccurrences(tupleListJson, "[-13007,200]") == ONE,
            "[-13007,200] appears multiple times");
        assertTrue(countOccurrences(tupleListJson, "[-61101,null]") == ONE,
            "[-61101,null] appears multiple times");
        assertTrue(countOccurrences(tupleListJson, "[-23101,null]") == ONE,
            "[-23101,null] appears multiple times");
    }

    @Test
    public void testCullDuplicates_NoIpErrors() {
        results.add(RDAPValidationResult.builder().code(-10000).build());

        results.cullDuplicateIPAddressErrors();

        assertThat(results.getAll()).hasSize(1);
        assertThat(results.getAll().stream().map(RDAPValidationResult::getCode))
            .containsExactly(-10000);
    }

    @Test
    public void testCullDuplicates_SingleIpv4() {
        results.add(RDAPValidationResult.builder().code(-20400).build());

        results.cullDuplicateIPAddressErrors();

        assertThat(results.getAll()).hasSize(1);
        assertThat(results.getAll().stream().map(RDAPValidationResult::getCode))
            .containsExactly(-20400);
    }

    @Test
    public void testCullDuplicates_SingleIpv6() {
        results.add(RDAPValidationResult.builder().code(-20401).build());

        results.cullDuplicateIPAddressErrors();

        assertThat(results.getAll()).hasSize(1);
        assertThat(results.getAll().stream().map(RDAPValidationResult::getCode))
            .containsExactly(-20401);
    }

    @Test
    public void testCullDuplicates_BothIpsOnce() {
        results.add(RDAPValidationResult.builder().code(-20400).build());
        results.add(RDAPValidationResult.builder().code(-20401).build());

        results.cullDuplicateIPAddressErrors();

        assertThat(results.getAll()).hasSize(2);
        assertThat(results.getAll().stream().map(RDAPValidationResult::getCode))
            .containsExactlyInAnyOrder(-20400, -20401);
    }

    @Test
    public void testCullDuplicates_DuplicateIpv4() {
        results.add(RDAPValidationResult.builder().code(-20400).build());
        results.add(RDAPValidationResult.builder().code(-20400).build());
        results.add(RDAPValidationResult.builder().code(-20401).build());

        results.cullDuplicateIPAddressErrors();

        assertThat(results.getAll()).hasSize(2);
        assertThat(results.getAll().stream().map(RDAPValidationResult::getCode))
            .containsExactlyInAnyOrder(-20400, -20401);
    }

    @Test
    public void testCullDuplicates_DuplicateIpv6() {
        results.add(RDAPValidationResult.builder().code(-20400).build());
        results.add(RDAPValidationResult.builder().code(-20401).build());
        results.add(RDAPValidationResult.builder().code(-20401).build());

        results.cullDuplicateIPAddressErrors();

        assertThat(results.getAll()).hasSize(2);
        assertThat(results.getAll().stream().map(RDAPValidationResult::getCode))
            .containsExactlyInAnyOrder(-20400, -20401);
    }

    @Test
    public void testCullDuplicates_BothDuplicates() {
        results.add(RDAPValidationResult.builder().code(-20400).build());
        results.add(RDAPValidationResult.builder().code(-20400).build());
        results.add(RDAPValidationResult.builder().code(-20401).build());
        results.add(RDAPValidationResult.builder().code(-20401).build());
        results.add(RDAPValidationResult.builder().code(-10000).build());

        results.cullDuplicateIPAddressErrors();

        assertThat(results.getAll()).hasSize(3);
        assertThat(results.getAll().stream().map(RDAPValidationResult::getCode))
            .containsExactlyInAnyOrder(-20400, -20401, -10000);
    }

    // Helper method to count occurrences in string
    private int countOccurrences(String str, String findStr) {
        int lastIndex = ZERO;
        int count = ZERO;
        while (lastIndex != -1) {
            lastIndex = str.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
        }
        return count;
    }
}