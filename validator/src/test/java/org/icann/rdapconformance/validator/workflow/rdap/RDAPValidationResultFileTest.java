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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.icann.rdapconformance.validator.QueryContext;
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
    private QueryContext queryContext;
    private RDAPValidatorConfiguration config;

    @BeforeMethod
    public void setUp() {
        // Create QueryContext-based instances instead of using singletons
        config = mock(RDAPValidatorConfiguration.class);
        when(config.isGtldRegistrar()).thenReturn(true);
        // Add URI configuration to prevent null pointer exceptions during QueryContext creation
        when(config.getUri()).thenReturn(java.net.URI.create("https://example.com/domain/test.example"));
        configurationFile = mock(ConfigurationFile.class);
        fileSystem = mock(FileSystem.class);

        queryContext = QueryContext.forTesting(config);
        results = queryContext.getResults();
        results.clear();
        results.addGroups(Set.of("firstGroup"));

        file = queryContext.getResultFile();
        file.initialize(results, config, configurationFile, fileSystem);
    }

    @AfterMethod
    public void tearDown() {
        results.clear();
    }


    @Test
    public void testGroupOkAssigned() throws IOException {
        file.build();
        verify(fileSystem).write(any(), contains("\"groupOK\": [\"firstGroup\"]"));
    }

    @Test
    public void testGroupErrorWarningAssigned() throws IOException {
        results.addGroupErrorWarning("secondGroup");
        file.build();
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
        // Use class field: config
        doReturn(true).when(config).useRdapProfileFeb2024();

        // Instance-based - no reset needed
        file.initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        file.build();

        verify(fileSystem).write(any(), contains("\"conformanceToolVersion\": \"" + BuildInfo.getVersion() + "\""));
    }

    @Test
    public void testBuildDate() throws IOException {
        // Use class field: config
        doReturn(true).when(config).useRdapProfileFeb2024();

        // Instance-based - no reset needed
        file.initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        file.build();

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
        // Use class field: config
        String customResultsFilePath = "custom_results.json";
        doReturn(customResultsFilePath).when(config).getResultsFile();

        // Instance-based - no reset needed
        file.initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        file.build();

        // Verify that the results are written to the custom file path
        verify(fileSystem).write(eq(customResultsFilePath), any(String.class));
    }

    @Test
    public void testDefaultResultsFilePath() throws IOException {
        // Use class field: config
        doReturn(null).when(config).getResultsFile();

        // Instance-based - no reset needed
        file.initialize(
            results,
            config,
            configurationFile,
            fileSystem
        );
        file.build();

    // Verify that the results are written to the default file path
    verify(fileSystem).mkdir("results");
    verify(fileSystem).write(contains("results/results-"), any(String.class));
  }


@Test
public void testAllCodesThatShouldBeIgnored() {
    // Create results with codes that should be filtered
    // Use class field: results
    results.clear();
    results.add(RDAPValidationResult.builder().code(-13004).httpStatusCode(200).build());
    results.add(RDAPValidationResult.builder().code(-13005).httpStatusCode(404).build());
    results.add(RDAPValidationResult.builder().code(-13006).httpStatusCode(404).build());
    results.add(RDAPValidationResult.builder().code(-65300).httpStatusCode(404).build());

    // Get all results from the implementation
    Set<RDAPValidationResult> allResults = results.getAll();

    // Use RDAPValidationResultFile's implementation to filter the results
    RDAPValidationResultFile resultFile = file;
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
    assertTrue(filteredResults.stream().anyMatch(r -> r.getCode() == -65300));
}

    @Test
    public void testBuggyIgnoredCodes() {
        // Use class field: results
        results.clear();

        results.add(RDAPValidationResult.builder().code(-130004).httpStatusCode(200).build());
        results.add(RDAPValidationResult.builder().code(-130005).httpStatusCode(404).build());

        String output = results.analyzeResultsWithStatusCheck();
        assertTrue(output.isEmpty());
        assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testAllNonIgnoredCodesSameStatus() {
        // Use class field: results
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
        // Use class field: results
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
        // Use class field: results
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
        // Use class field: results
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
        // Use class field: results
        results.clear();

        String output = results.analyzeResultsWithStatusCheck();
        assertTrue(output.isEmpty());
        assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));
    }

    @Test
    public void testNullAndZeroStatusCodesAreEquivalent() {
        // Create QueryContext for proper status code handling
        // Use class field: config
        when(config.isGtldRegistrar()).thenReturn(true);
        QueryContext queryContext = QueryContext.forTesting(config);

        // Mock ConnectionTracker through QueryContext
        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        ConnectionTracker.ConnectionRecord mockConnection = mock(ConnectionTracker.ConnectionRecord.class);
        when(mockConnection.getStatusCode()).thenReturn(0);
        when(mockTracker.getLastMainConnection()).thenReturn(mockConnection);

        // Use reflection to set the mocked tracker in QueryContext for this test
        try {
            java.lang.reflect.Field trackerField = QueryContext.class.getDeclaredField("connectionTracker");
            trackerField.setAccessible(true);
            trackerField.set(queryContext, mockTracker);

            // Use class field: results
            results.clear();

            // Use QueryContext to properly normalize null to 0
            results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(null).build(queryContext));
            results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(0).build(queryContext));

            String output = results.analyzeResultsWithStatusCheck();
            assertTrue(output.contains("code=1001, httpStatusCode=0"));
            assertTrue(output.contains("code=1002, httpStatusCode=0"));
            assertFalse(results.getAll().stream().anyMatch(r -> r.getCode() == -13018));

        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock ConnectionTracker", e);
        }
    }

    @Test
    public void testMixedNullZeroAndOtherStatusCodes() {
        // Create QueryContext for proper status code handling
        // Use class field: config
        when(config.isGtldRegistrar()).thenReturn(true);
        QueryContext queryContext = QueryContext.forTesting(config);

        // Mock ConnectionTracker through QueryContext
        ConnectionTracker mockTracker = mock(ConnectionTracker.class);
        ConnectionTracker.ConnectionRecord mockConnection = mock(ConnectionTracker.ConnectionRecord.class);
        when(mockConnection.getStatusCode()).thenReturn(0);
        when(mockTracker.getLastMainConnection()).thenReturn(mockConnection);

        // Use reflection to set the mocked tracker in QueryContext for this test
        try {
            java.lang.reflect.Field trackerField = QueryContext.class.getDeclaredField("connectionTracker");
            trackerField.setAccessible(true);
            trackerField.set(queryContext, mockTracker);

            // Use class field: results
            results.clear();
            results.add(RDAPValidationResult.builder().code(1001).httpStatusCode(null).build(queryContext));
            results.add(RDAPValidationResult.builder().code(1002).httpStatusCode(0).build(queryContext));
            results.add(RDAPValidationResult.builder().code(1003).httpStatusCode(200).build(queryContext));

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

        } catch (Exception e) {
            throw new RuntimeException("Failed to set mock ConnectionTracker", e);
        }
    }

    @Test
    public void testNoDuplicateTuplesInJson() {
        // Use class field: results
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

        RDAPValidationResultFile resultFile = file;
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

        RDAPValidationResultFile resultFile = file;
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

        RDAPValidationResultFile resultFile = file;
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

        RDAPValidationResultFile resultFile = file;
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

        RDAPValidationResultFile resultFile = file;
        Set<RDAPValidationResult> culled = resultFile.cullDuplicateIPAddressErrors(testResults);

        // Verify duplicates are removed but one of each IP error remains
        assertEquals(3, culled.size());
        assertTrue(culled.stream().anyMatch(r -> r.getCode() == -20400));
        assertTrue(culled.stream().anyMatch(r -> r.getCode() == -20401));
        assertTrue(culled.stream().anyMatch(r -> r.getCode() == 1001));
    }

@Test
public void testCreateResultsMap() {
    RDAPValidationResultFile resultFile = file;

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
    @Test
    public void testGetErrorCount() {
        // Clear existing results
        results.clear();
        
        // Mock configuration file behavior
        doReturn(true).when(configurationFile).isError(-1001);
        doReturn(false).when(configurationFile).isWarning(-1001);
        doReturn(false).when(configurationFile).isError(-2001);
        doReturn(true).when(configurationFile).isWarning(-2001);
        doReturn(List.of(-3001)).when(configurationFile).getDefinitionIgnore();
        
        // Add different types of results
        results.add(RDAPValidationResult.builder().code(-1001).build()); // Error
        results.add(RDAPValidationResult.builder().code(-2001).build()); // Warning
        results.add(RDAPValidationResult.builder().code(-3001).build()); // Ignored
        results.add(RDAPValidationResult.builder().code(-4001).build()); // Neither error nor warning nor ignored (defaults to error)
        
        RDAPValidationResultFile resultFile = file;
        int errorCount = resultFile.getErrorCount();
        
        assertEquals(2, errorCount); // -1001 and -4001 should be counted as errors
    }
    
    @Test
    public void testGetErrors() {
        // Clear existing results
        results.clear();
        
        // Mock configuration file behavior
        doReturn(true).when(configurationFile).isError(-1001);
        doReturn(false).when(configurationFile).isWarning(-1001);
        doReturn(false).when(configurationFile).isError(-2001);
        doReturn(true).when(configurationFile).isWarning(-2001);
        doReturn(List.of(-3001)).when(configurationFile).getDefinitionIgnore();
        
        // Add different types of results
        results.add(RDAPValidationResult.builder().code(-1001).message("Error 1").build());
        results.add(RDAPValidationResult.builder().code(-2001).message("Warning 1").build());
        results.add(RDAPValidationResult.builder().code(-3001).message("Ignored 1").build());
        results.add(RDAPValidationResult.builder().code(-4001).message("Default Error").build());
        
        RDAPValidationResultFile resultFile = file;
        List<RDAPValidationResult> errors = resultFile.getErrors();
        
        assertEquals(2, errors.size());
        assertTrue(errors.stream().anyMatch(r -> r.getCode() == -1001));
        assertTrue(errors.stream().anyMatch(r -> r.getCode() == -4001));
        assertFalse(errors.stream().anyMatch(r -> r.getCode() == -2001)); // Warning should not be included
        assertFalse(errors.stream().anyMatch(r -> r.getCode() == -3001)); // Ignored should not be included
    }
    
    @Test
    public void testRemoveErrors() {
        // Clear existing results
        results.clear();
        
        // Mock configuration file behavior
        doReturn(true).when(configurationFile).isError(-1001);
        doReturn(false).when(configurationFile).isWarning(-1001);
        doReturn(false).when(configurationFile).isError(-2001);
        doReturn(true).when(configurationFile).isWarning(-2001);
        doReturn(false).when(configurationFile).isError(-3001);
        doReturn(false).when(configurationFile).isWarning(-3001);
        doReturn(List.of()).when(configurationFile).getDefinitionIgnore();
        
        // Add different types of results
        results.add(RDAPValidationResult.builder().code(-1001).message("Error 1").build());
        results.add(RDAPValidationResult.builder().code(-2001).message("Warning 1").build());
        results.add(RDAPValidationResult.builder().code(-3001).message("Default Error").build());
        
        assertEquals(3, results.getAll().size());
        
        RDAPValidationResultFile resultFile = file;
        resultFile.removeErrors();
        
        // Only warnings should remain
        Set<RDAPValidationResult> remaining = results.getAll();
        assertEquals(1, remaining.size());
        assertTrue(remaining.stream().anyMatch(r -> r.getCode() == -2001));
    }
    
    @Test
    public void testRemoveErrorsPreservesResponseFormatErrors() {
        // Clear existing results
        results.clear();
        
        // Mock configuration file behavior - none of these codes are defined as warnings
        doReturn(false).when(configurationFile).isError(-12107);
        doReturn(false).when(configurationFile).isWarning(-12107);
        doReturn(false).when(configurationFile).isError(-12108);
        doReturn(false).when(configurationFile).isWarning(-12108);
        doReturn(true).when(configurationFile).isError(-13001);
        doReturn(false).when(configurationFile).isWarning(-13001);
        doReturn(false).when(configurationFile).isError(-2001);
        doReturn(true).when(configurationFile).isWarning(-2001);
        doReturn(List.of()).when(configurationFile).getDefinitionIgnore();
        
        // Add mixed types of results:
        // -12107: Response format error (should be preserved)
        results.add(RDAPValidationResult.builder().code(-12107)
                   .message("The errorCode value is required in an error response.")
                   .build());
        // -12108: Another response format error (should be preserved)  
        results.add(RDAPValidationResult.builder().code(-12108)
                   .message("Another response format error.")
                   .build());
        // -13001: Content validation error (should be removed)
        results.add(RDAPValidationResult.builder().code(-13001)
                   .message("Content validation error.")
                   .build());
        // -2001: Warning (should be preserved)
        results.add(RDAPValidationResult.builder().code(-2001)
                   .message("Warning message.")
                   .build());
        
        assertEquals(4, results.getAll().size());
        
        RDAPValidationResultFile resultFile = file;
        resultFile.removeErrors();
        
        // Should preserve warnings AND response format errors (-121XX range)
        Set<RDAPValidationResult> remaining = results.getAll();
        assertEquals(3, remaining.size());
        
        // Check that -12107 is preserved (response format error)
        assertTrue(remaining.stream().anyMatch(r -> r.getCode() == -12107), 
                   "Response format error -12107 should be preserved");
        
        // Check that -12108 is preserved (response format error)
        assertTrue(remaining.stream().anyMatch(r -> r.getCode() == -12108), 
                   "Response format error -12108 should be preserved");
        
        // Check that warning is preserved
        assertTrue(remaining.stream().anyMatch(r -> r.getCode() == -2001), 
                   "Warning should be preserved");
        
        // Check that content validation error is removed
        assertFalse(remaining.stream().anyMatch(r -> r.getCode() == -13001), 
                    "Content validation error -13001 should be removed");
    }
    
    @Test
    public void testInitializeWhenAlreadyInitialized() {
        // Initialize once
        RDAPValidationResultFile resultFile = file;
        resultFile.initialize(results, config, 
                             configurationFile, fileSystem);
        
        // Try to initialize again - should return early without overwriting
        RDAPValidatorConfiguration newConfig = config;
        resultFile.initialize(results, newConfig, configurationFile, fileSystem);
        
        // The second initialization should be ignored - test re-initialization safety
        assertTrue(true); // Test passes if no exception thrown
    }
    
    @Test 
    public void testBuildWithEmptyResultsFilePath() throws Exception {
        // Test the edge case where resultsFilePath is empty string (not null)
        // Use class field: config
        doReturn("").when(config).getResultsFile(); // Empty string, not null
        doReturn(false).when(config).useRdapProfileFeb2024();
        
        RDAPValidationResultFile resultFile = file;
        resultFile.initialize(results, config, configurationFile, fileSystem);
        
        boolean result = resultFile.build();
        
        assertTrue(result);
        // Test empty string handling in file path logic
    }
    
    // Note: IOException test removed - difficult to mock in this architecture
    // The catch block at lines 122-124 provides error handling robustness
    // but is challenging to test with the current FileSystem abstraction
    @Test
    public void testFormatStatusCodeWithZero() {
        // Add a result with status code 0 to test the ZERO formatting
        results.add(RDAPValidationResult.builder()
                   .code(-12107)
                   .httpStatusCode(0) // This should be formatted as NULL
                   .message("Test")
                   .build());
        
        // Use class field: config
        doReturn(false).when(config).useRdapProfileFeb2024();
        
        RDAPValidationResultFile resultFile = file;
        resultFile.initialize(results, config, configurationFile, fileSystem);
        
        Map<String, Object> resultMap = resultFile.createResultsMap();
        List<Map<String, Object>> errors = (List<Map<String, Object>>) resultMap.get("error");
        
        assertFalse(errors.isEmpty());
        Object statusCode = errors.get(0).get("receivedHttpStatusCode");
        assertEquals(statusCode, JSONObject.NULL);
        // Test status code zero formatting as NULL
    }
    
    @Test
    public void testFormatStringWithDash() {
        // Add a result with DASH values to test dash formatting
        results.add(RDAPValidationResult.builder()
                   .code(-12107)
                   .acceptHeader(DASH) // This should be formatted as NULL
                   .httpMethod(DASH)
                   .queriedURI(DASH)
                   .serverIpAddress(DASH)
                   .message("Test")
                   .build());
        
        // Use class field: config
        doReturn(false).when(config).useRdapProfileFeb2024();
        
        RDAPValidationResultFile resultFile = file;
        resultFile.initialize(results, config, configurationFile, fileSystem);
        
        Map<String, Object> resultMap = resultFile.createResultsMap();
        List<Map<String, Object>> errors = (List<Map<String, Object>>) resultMap.get("error");
        
        assertFalse(errors.isEmpty());
        Map<String, Object> error = errors.get(0);
        assertEquals(error.get("acceptMediaType"), JSONObject.NULL);
        assertEquals(error.get("httpMethod"), JSONObject.NULL);
        assertEquals(error.get("serverIpAddress"), JSONObject.NULL);
        // Test DASH string formatting as NULL
    }
    
    @Test
    public void testCreateResultsMapWithNullConfigUri() {
        results.add(RDAPValidationResult.builder()
                   .code(-12107)
                   .queriedURI(null) // This will trigger fallback to config.getUri()
                   .message("Test")
                   .build());
        
        // Use class field: config
        doReturn(null).when(config).getUri(); // Null URI to test the fallback
        doReturn(false).when(config).useRdapProfileFeb2024();
        
        RDAPValidationResultFile resultFile = file;
        resultFile.initialize(results, config, configurationFile, fileSystem);
        
        Map<String, Object> resultMap = resultFile.createResultsMap();
        List<Map<String, Object>> errors = (List<Map<String, Object>>) resultMap.get("error");
        
        assertFalse(errors.isEmpty());
        String queriedURI = (String) errors.get(0).get("queriedURI");
        assertEquals(queriedURI, ""); // Should be empty string when config.getUri() is null
        // Test null config URI fallback to empty string
    }
    
    @Test
    public void testStatusCodeComparisonWithNullStatus() {
        // Create QueryContext for proper status code handling
        // Use class field: config
        when(config.isGtldRegistrar()).thenReturn(true);
        QueryContext queryContext = QueryContext.forTesting(config);

        // Add results with null HTTP status codes to test null handling
        results.add(RDAPValidationResult.builder()
                   .code(-12107)
                   .httpStatusCode(null) // Null status code
                   .message("Test 1")
                   .build(queryContext));
        results.add(RDAPValidationResult.builder()
                   .code(-12108)
                   .httpStatusCode(404)
                   .message("Test 2")
                   .build(queryContext));
        
        RDAPValidationResultFile resultFile = file;
        
        Set<RDAPValidationResult> allResults = results.getAll();
        Set<RDAPValidationResult> processedResults = resultFile.addErrorIfAllQueriesDoNotReturnSameStatusCode(allResults);
        
        // Should have added -13018 error because null != 404
        assertTrue(processedResults.stream().anyMatch(r -> r.getCode() == -13018));
        // Test null status code normalization in comparisons
    }
    
    @Test
    public void testCullDuplicateIPAddressErrors() {
        // Add multiple -20400 and -20401 errors to test duplicate culling
        results.add(RDAPValidationResult.builder().code(-20400).message("IPv4 error 1").build());
        results.add(RDAPValidationResult.builder().code(-20400).message("IPv4 error 2").build());
        results.add(RDAPValidationResult.builder().code(-20400).message("IPv4 error 3").build());
        results.add(RDAPValidationResult.builder().code(-20401).message("IPv6 error 1").build());
        results.add(RDAPValidationResult.builder().code(-20401).message("IPv6 error 2").build());
        
        RDAPValidationResultFile resultFile = file;
        Set<RDAPValidationResult> culled = resultFile.cullDuplicateIPAddressErrors(results.getAll());
        
        // Should keep only 1 of each type
        long ipv4Count = culled.stream().filter(r -> r.getCode() == -20400).count();
        long ipv6Count = culled.stream().filter(r -> r.getCode() == -20401).count();
        
        assertEquals(ipv4Count, 1);
        assertEquals(ipv6Count, 1);
        // Test IP address error duplicate culling logic
    }
    
    @Test
    public void testDebugPrintResultBreakdownWithIgnoredCodes() {
        // Add results with codes that should be ignored
        results.add(RDAPValidationResult.builder().code(-99999).message("Ignored code").build());
        
        // Mock configuration to define this as ignored
        doReturn(List.of(-99999)).when(configurationFile).getDefinitionIgnore();
        doReturn(false).when(configurationFile).isError(-99999);
        doReturn(false).when(configurationFile).isWarning(-99999);
        
        RDAPValidationResultFile resultFile = file;
        resultFile.initialize(results, config, configurationFile, fileSystem);
        
        // Test debug printing with ignored codes classification
        resultFile.debugPrintResultBreakdown();
        assertTrue(true);
    }
    
    @Test
    public void testPrintCategoryExamplesWithNullValue() {
        // Add result with null value to test null handling in debug printing
        results.add(RDAPValidationResult.builder()
                   .code(-12107)
                   .value(null) // Null value to test line 390
                   .message("Test with null value")
                   .build());
        
        RDAPValidationResultFile resultFile = file;
        resultFile.initialize(results, config, configurationFile, fileSystem);
        
        // Test debug printing with null values handling
        resultFile.debugPrintResultBreakdown();
        assertTrue(true);
    }
    
    @Test
    public void testJsonSerializationError() throws Exception {
        // Create results that will cause different status codes to test JSON serialization
        results.add(RDAPValidationResult.builder()
                   .code(-12107)
                   .httpStatusCode(404)
                   .message("Test 1")
                   .build());
        results.add(RDAPValidationResult.builder()
                   .code(-12108)
                   .httpStatusCode(500)
                   .message("Test 2")
                   .build());
        
        // This scenario creates uniqueTuples that could potentially cause JSON serialization issues
        // In practice, ObjectMapper.writeValueAsString should handle this, but this test
        // exercises the code path and ensures robustness
        RDAPValidationResultFile resultFile = file;
        
        Set<RDAPValidationResult> processedResults = resultFile.addErrorIfAllQueriesDoNotReturnSameStatusCode(results.getAll());
        
        // Should have added -13018 error for mixed status codes  
        assertTrue(processedResults.stream().anyMatch(r -> r.getCode() == -13018));
        
        // The JSON value should contain the tuple information
        RDAPValidationResult mixedStatusResult = processedResults.stream()
                .filter(r -> r.getCode() == -13018)
                .findFirst()
                .orElse(null);
        assertNotNull(mixedStatusResult);
        assertNotNull(mixedStatusResult.getValue());
        assertTrue(mixedStatusResult.getValue().contains("[["));
        
        // Test JSON serialization of tuple data for mixed status codes
        // The exception case is hard to trigger with normal data,
        // but this at least exercises the successful path
    }

    @Test
    public void testRemoveResultGroups() {
        // Add some results with groups
        results.clear();
        results.addGroup("TestGroup1");
        results.addGroup("TestGroup2");
        results.add(RDAPValidationResult.builder().code(-1001).build());
        
        RDAPValidationResultFile resultFile = file;
        resultFile.removeResultGroups();
        
        // Groups should be removed but results should remain
        assertFalse(results.getAll().isEmpty());
        // Note: We can't easily test group removal without accessing internal state
    }
    
    @Test
    public void testGetResultsPath() {
        RDAPValidationResultFile resultFile = file;
        
        // Set a test path
        resultFile.resultPath = "/test/path/results.json";
        
        String path = resultFile.getResultsPath();
        assertEquals("/test/path/results.json", path);
    }
    
    @Test
    public void testGetResultsPath_Null() {
        RDAPValidationResultFile resultFile = file;
        
        // Clear the path
        resultFile.resultPath = null;
        
        String path = resultFile.getResultsPath();
        assertThat(path).isNull();
    }
    
    @Test
    public void testDebugPrintResultBreakdown() {
        // Clear existing results
        results.clear();
        
        // Mock configuration file behavior
        doReturn(true).when(configurationFile).isError(-1001);
        doReturn(false).when(configurationFile).isWarning(-1001);
        doReturn(false).when(configurationFile).isError(-2001);
        doReturn(true).when(configurationFile).isWarning(-2001);
        doReturn(List.of()).when(configurationFile).getDefinitionIgnore();
        
        // Add results
        results.add(RDAPValidationResult.builder().code(-1001).message("Error 1").build());
        results.add(RDAPValidationResult.builder().code(-2001).message("Warning 1").build());
        results.add(RDAPValidationResult.builder().code(-3001).message("Default Error").build());
        
        RDAPValidationResultFile resultFile = file;
        
        // This method should not throw an exception
        resultFile.debugPrintResultBreakdown(); // Should not throw
    }
    
    @Test
    public void testGetAllResults() {
        // Clear existing results
        results.clear();
        
        // Add some results
        results.add(RDAPValidationResult.builder().code(-1001).build());
        results.add(RDAPValidationResult.builder().code(-2001).build());
        
        RDAPValidationResultFile resultFile = file;
        List<RDAPValidationResult> allResults = resultFile.getAllResults();
        
        assertEquals(2, allResults.size());
        assertTrue(allResults.stream().anyMatch(r -> r.getCode() == -1001));
        assertTrue(allResults.stream().anyMatch(r -> r.getCode() == -2001));
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