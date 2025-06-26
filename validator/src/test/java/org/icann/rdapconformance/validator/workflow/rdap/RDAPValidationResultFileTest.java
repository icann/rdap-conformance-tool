package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.icann.rdapconformance.validator.exception.parser.ExceptionParser.UNKNOWN_ERROR_CODE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.BuildInfo;
import org.icann.rdapconformance.validator.ConnectionTracker;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.json.JSONObject;
import org.mockito.MockedStatic;
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
public void testAllCodesThatShouldBeIgnored() {
    // Create results with codes that should be filtered
    RDAPValidatorResultsImpl resultsImpl = RDAPValidatorResultsImpl.getInstance();
    resultsImpl.clear();
    resultsImpl.add(RDAPValidationResult.builder().code(-13004).httpStatusCode(200).build());
    resultsImpl.add(RDAPValidationResult.builder().code(-13005).httpStatusCode(404).build());
    resultsImpl.add(RDAPValidationResult.builder().code(-13006).httpStatusCode(404).build());
    resultsImpl.add(RDAPValidationResult.builder().code(-46701).httpStatusCode(404).build());

    // Get all results from the implementation
    Set<RDAPValidationResult> allResults = resultsImpl.getAll();

    // Use RDAPValidationResultFile's implementation to filter the results
    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
    Set<RDAPValidationResult> filteredResults = resultFile.addErrorIfAllQueriesDoNotReturnSameStatusCode(allResults);

    // Check that the filtered codes don't appear in the unique tuples that are checked
    // for status code differences (this is what the method actually does)
    assertFalse(filteredResults.stream().anyMatch(r -> r.getCode() == -13018),
        "No -13018 error should be added when only filtered codes are present");

    // The original codes should still be in the results set
    assertEquals(4, filteredResults.size(), "Original results should remain in the set");
    assertTrue(filteredResults.stream().anyMatch(r -> r.getCode() == -13004));
    assertTrue(filteredResults.stream().anyMatch(r -> r.getCode() == -13005));
    assertTrue(filteredResults.stream().anyMatch(r -> r.getCode() == -13006));
    assertTrue(filteredResults.stream().anyMatch(r -> r.getCode() == -46701));
}

    @Test
    public void testBuggyIgnoredCodes() {
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
        assertTrue(tupleResult.getValue().contains("[[1001,0],[1002,200]]") ||
            tupleResult.getValue().contains("[[1002,200],[1001,0]]"));
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
        try (MockedStatic<ConnectionTracker> mocked = org.mockito.Mockito.mockStatic(ConnectionTracker.class)) {
            mocked.when(ConnectionTracker::getMainStatusCode).thenReturn(0);

            RDAPValidatorResultsImpl results = RDAPValidatorResultsImpl.getInstance();
            results.clear();

            results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(null).build());
            results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(0).build());

            String output = results.analyzeResultsWithStatusCheck();
            assertTrue(output.contains("code=1001, httpStatusCode=0"));
            assertTrue(output.contains("code=1002, httpStatusCode=0"));
            assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
        }
    }

    @Test
    public void testMixedNullZeroAndOtherStatusCodes() {
        try (MockedStatic<ConnectionTracker> mocked = org.mockito.Mockito.mockStatic(ConnectionTracker.class)) {
            mocked.when(ConnectionTracker::getMainStatusCode).thenReturn(0);

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

            System.out.println("Tuple result: " + tupleResult);
            assertNotNull(tupleResult);
            String value = tupleResult.getValue();
            assertTrue(value.contains("[1001,0]"));
            assertTrue(value.contains("[1002,0]"));
            assertTrue(value.contains("[1003,200]"));
        }
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
        results.add(RDAPValidationResult.builder().code(-61101).httpStatusCode(ZERO).build());
        results.add(RDAPValidationResult.builder().code(-61101).httpStatusCode(ZERO).build());
        results.add(RDAPValidationResult.builder().code(-23101).httpStatusCode(ZERO).build());
        results.add(RDAPValidationResult.builder().code(-23101).httpStatusCode(ZERO).build());

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
        assertTrue(countOccurrences(tupleListJson, "[-61101,0]") == ONE,
            "[-61101,0] appears multiple times");
        assertTrue(countOccurrences(tupleListJson, "[-23101,0]") == ONE,
            "[-23101,0] appears multiple times");
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

    @Test
    public void testAnalyzeResultsWithStatusCheck_EmptyResults() {
        // Create nothing - get nothing
        Set<RDAPValidationResult> testResults = new HashSet<>();

        RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
        Set<RDAPValidationResult> filtered = resultFile.addErrorIfAllQueriesDoNotReturnSameStatusCode(testResults);
        assertTrue(filtered.isEmpty());
    }

    @Test
    public void testAddErrorIfAllQueriesDoNotReturnSame_StatusCode_UniqueTuples() {
        // Tests creating unique tuples with duplicate code/status combinations
        Set<RDAPValidationResult> testResults = new HashSet<>();
        testResults.add(RDAPValidationResult.builder().code(1001).httpStatusCode(200).build());
        testResults.add(RDAPValidationResult.builder().code(1001).httpStatusCode(200).build()); // Duplicate
        testResults.add(RDAPValidationResult.builder().code(1002).httpStatusCode(404).build());

        RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
        Set<RDAPValidationResult> filtered = resultFile.addErrorIfAllQueriesDoNotReturnSameStatusCode(testResults);

        // Verify all unique code/status combinations are preserved
        assertTrue(filtered.stream().anyMatch(r -> r.getCode() == 1001 && r.getHttpStatusCode() == 200));
        assertTrue(filtered.stream().anyMatch(r -> r.getCode() == 1002 && r.getHttpStatusCode() == 404));

        // Verify the -13018 code is added for different status codes
        assertTrue(filtered.stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testAddErrorIfAllStatusCheck_StatusCodeNormalization() {
        // Test Status code normalization (null to 0)
        Set<RDAPValidationResult> testResults = new HashSet<>();
        testResults.add(RDAPValidationResult.builder().code(1001).httpStatusCode(0).build());
        testResults.add(RDAPValidationResult.builder().code(1002).httpStatusCode(0).build());

        RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
        Set<RDAPValidationResult> filtered = resultFile.addErrorIfAllQueriesDoNotReturnSameStatusCode(testResults);

        // Verify results are maintained
        assertEquals(2, filtered.size());

        // Verify no -13018 code is added since null and 0 are considered equivalent
        assertFalse(filtered.stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testAddErrorIfAllStatusCheck_DifferentStatusCodes() {
        // Tests adding error code for different status codes
        Set<RDAPValidationResult> testResults = new HashSet<>();
        testResults.add(RDAPValidationResult.builder().code(1001).httpStatusCode(200).build());
        testResults.add(RDAPValidationResult.builder().code(1002).httpStatusCode(404).build());

        RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
        Set<RDAPValidationResult> filtered = resultFile.addErrorIfAllQueriesDoNotReturnSameStatusCode(testResults);

        // Verify the -13018 code is added
        assertTrue(filtered.stream().anyMatch(r -> r.getCode() == -13018));

        // Verify the error message contains the tuples
        Optional<RDAPValidationResult> errorResult = filtered.stream()
                                                             .filter(r -> r.getCode() == -13018)
                                                             .findFirst();
        assertTrue(errorResult.isPresent());
        String value = errorResult.get().getValue();
        assertTrue(value.contains("[1001,200]"));
        assertTrue(value.contains("[1002,404]"));
    }

    @Test
    public void testCullDuplicateIPAddressErrors_Implementation() {
        // Test IP address error culling logic
        Set<RDAPValidationResult> testResults = new HashSet<>();
        testResults.add(RDAPValidationResult.builder().code(-20400).build()); // IPv4 error
        testResults.add(RDAPValidationResult.builder().code(-20400).build()); // Duplicate IPv4 error
        testResults.add(RDAPValidationResult.builder().code(-20401).build()); // IPv6 error
        testResults.add(RDAPValidationResult.builder().code(-20401).build()); // Duplicate IPv6 error
        testResults.add(RDAPValidationResult.builder().code(1001).build());   // Other code

        RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();
        Set<RDAPValidationResult> culled = resultFile.cullDuplicateIPAddressErrors(testResults);

        // Verify duplicates are removed but one of each IP error remains
        assertEquals(3, culled.size());
        assertTrue(culled.stream().anyMatch(r -> r.getCode() == -20400));
        assertTrue(culled.stream().anyMatch(r -> r.getCode() == -20401));
        assertTrue(culled.stream().anyMatch(r -> r.getCode() == 1001));
    }

@Test
public void testCreateResultsMap() {
    RDAPValidationResultFile resultFile = RDAPValidationResultFile.getInstance();

    // Mock configuration file behavior
    doReturn(List.of(999)).when(configurationFile).getDefinitionIgnore();

    // Setup specific behaviors for isError and isWarning
    doReturn(true).when(configurationFile).isError(-1001); // This is an error
    doReturn(true).when(configurationFile).isWarning(-2001); // This is a warning
    doReturn(false).when(configurationFile).isError(-3001); // Not explicitly an error
    doReturn(false).when(configurationFile).isWarning(-3001); // Not explicitly a warning
    doReturn(true).when(configurationFile).isError(-13018); // The mixed status code error

    doReturn("Error note").when(configurationFile).getAlertNotes(-1001);
    doReturn("Warning note").when(configurationFile).getAlertNotes(-2001);
    doReturn("Unknown note").when(configurationFile).getAlertNotes(-3001);
    doReturn("Status code error").when(configurationFile).getAlertNotes(-13018);

    // make sure the results are cleared before starting
    results.clear();

    // Add an error with all fields populated
    results.add(RDAPValidationResult.builder()
        .code(-1001)
        .value("error-value")
        .message("error-message")
        .httpStatusCode(200)
        .queriedURI("https://example.com/error")
        .acceptHeader("application/rdap+json")
        .httpMethod("GET")
        .serverIpAddress("192.168.1.1")
        .build());

    // Add a warning with some null fields
    results.add(RDAPValidationResult.builder()
        .code(-2001)
        .value("warning-value")
        .message("warning-message")
        .httpStatusCode(404) // Different status code
        .queriedURI(null)
        .acceptHeader(DASH) // This should be converted to null
        .httpMethod(null)
        .serverIpAddress(null)
        .build());

    // Add an unknown type with minimal fields
    results.add(RDAPValidationResult.builder()
        .code(-3001)
        .value("unknown-value")
        .message("unknown-message")
        .httpStatusCode(500) // Another different status code
        .build());

    // Call it!
    Map<String, Object> resultsMap = resultFile.createResultsMap();

    // Verify results
    assertNotNull(resultsMap);

    // Verify ignore list
    assertEquals(List.of(999), resultsMap.get("ignore"));

    // Extract and verify error entries
    List<Map<String, Object>> errors = (List<Map<String, Object>>) resultsMap.get("error");

    // We expect 3 errors:
    // the original error (-1001),
    // the unknown type that defaults to error (-3001),
    // and the mixed status code error (-13018)
    assertEquals(3, errors.size(), "Expected 3 error entries: -1001, -3001, and -13018");

    // Verify we have all expected error codes
    Set<Integer> errorCodes = errors.stream()
        .map(map -> (Integer)map.get("code"))
        .collect(Collectors.toSet());

    assertTrue(errorCodes.contains(-1001), "Error list should contain code -1001");
    assertTrue(errorCodes.contains(-3001), "Error list should contain code -3001");
    assertTrue(errorCodes.contains(-13018), "Error list should contain code -13018 for mixed status codes");

    // Verify error entry with all fields
    Map<String, Object> errorEntry = errors.stream()
        .filter(map -> map.get("code").equals(-1001))
        .findFirst()
        .orElse(null);
    assertNotNull(errorEntry, "Error entry for code -1001 should exist");
    assertEquals("error-value", errorEntry.get("value"));
    assertEquals("error-message", errorEntry.get("message"));
    assertEquals(200, errorEntry.get("receivedHttpStatusCode"));
    assertEquals("https://example.com/error", errorEntry.get("queriedURI"));
    assertEquals("application/rdap+json", errorEntry.get("acceptMediaType"));
    assertEquals("GET", errorEntry.get("httpMethod"));
    assertEquals("192.168.1.1", errorEntry.get("serverIpAddress"));
    assertEquals("Error note", errorEntry.get("notes"));

    // Extract and verify warning entries
    List<Map<String, Object>> warnings = (List<Map<String, Object>>) resultsMap.get("warning");
    assertEquals(1, warnings.size(), "Should have 1 warning entry");

    // Verify warning entry with some null fields
    Map<String, Object> warningEntry = warnings.get(0);
    assertEquals(-2001, warningEntry.get("code"));
    assertEquals("warning-value", warningEntry.get("value"));
    assertEquals("warning-message", warningEntry.get("message"));
    assertEquals(404, warningEntry.get("receivedHttpStatusCode"));
    assertEquals(JSONObject.NULL, warningEntry.get("acceptMediaType"));
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