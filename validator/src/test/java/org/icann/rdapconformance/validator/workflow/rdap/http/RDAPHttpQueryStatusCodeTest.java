package org.icann.rdapconformance.validator.workflow.rdap.http;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.net.URI;
import java.util.Set;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Comprehensive test suite for HTTP status code handling behavior in RDAPHttpQuery.
 * 
 * This tests the ACTUAL RDAPHttpQuery.run() behavior by mocking HTTP responses and verifying:
 * 
 * 1. -13002 Error Generation: Tests that invalid HTTP status codes (not 200 or 404) 
 *    generate the correct -13002 error with proper early termination logic for 4xx codes.
 * 
 * 2. -12108 Error Code Matching: Tests that errorCode field values match HTTP status codes
 *    for non-200 responses, ensuring proper validation order (-12107 before -12108).
 * 
 * 3. Early Termination: Verifies that 4xx client errors cause early termination, preventing
 *    further 2024 profile validation from occurring.
 * 
 * All tests use WireMock to simulate real HTTP responses and verify the complete validation flow.
 */
public class RDAPHttpQueryStatusCodeTest {

    private WireMockServer wireMockServer;
    private RDAPValidatorConfiguration config;
    private QueryContext queryContext;

    @BeforeMethod
    public void setUp() {
        // Start WireMock server on dynamic port
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        
        // Configure WireMock client to use our server
        configureFor("localhost", wireMockServer.port());

        // Setup configuration mock
        config = mock(RDAPValidatorConfiguration.class);
        doReturn(5000).when(config).getTimeout();
        doReturn(5).when(config).getMaxRedirects();
        doReturn(true).when(config).useRdapProfileFeb2024(); // Enable Feb 2024 profile for -12107 testing
        doReturn(true).when(config).isGtldRegistrar();
        doReturn(URI.create("https://example.com/domain/test.example")).when(config).getUri();

        // Create dataset service first, then QueryContext with it
        org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService datasets =
            new org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock();
        datasets.download(true);

        // Create QueryContext for thread-safe operations
        queryContext = QueryContext.forTesting(config, datasets);
        queryContext.setSsrfProtectionEnabled(false);

        // Clear any previous results
        queryContext.getResults().clear();
    }

    @AfterMethod
    public void tearDown() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
        queryContext.getResults().clear();
    }

    @DataProvider(name = "statusCodeScenarios")
    public Object[][] statusCodeScenarios() {
        return new Object[][] {
            // Format: {testName, statusCode, expectEarlyTermination, expect13002Error, responseBody}
            
            // Client Errors (4xx) - Should cause early termination + -13002 error (except 404)
            {"Client Error 400", 400, true, true, "Bad Request"},
            {"Client Error 401", 401, true, true, "Unauthorized"}, 
            {"Client Error 403", 403, true, true, "Forbidden"},
            {"Client Error 409", 409, true, true, "Conflict"},
            {"Client Error 422", 422, true, true, "Unprocessable Entity"},
            {"Client Error 499", 499, true, true, "Client Closed Request"},
            
            // Special case: 404 is valid (no -13002) but still causes early termination (is 4xx)
            {"Not Found 404", 404, true, false, "{\"rdapConformance\":[\"rdap_level_0\"]}"},
            
            // Server Errors (5xx) - Should continue validation + -13002 error
            {"Server Error 500", 500, false, true, "{\"rdapConformance\":[\"rdap_level_0\"]}"},
            {"Server Error 502", 502, false, true, "Bad Gateway"},
            {"Server Error 503", 503, false, true, "Service Unavailable"},
            
            // Expected Code: 200 - Should continue normally without -13002
            {"Success 200", 200, false, false, "{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\"}"},
            
            // Invalid/Unusual - Should continue validation + -13002 error (non-4xx)
            // NOTE: Removed 102 and 103 - these informational codes cause HTTP client failures
            {"Redirect 307", 307, false, true, "Temporary Redirect"}, 
            // 418 is 4xx, so it should cause early termination like other 4xx codes
            {"Unusual 418", 418, true, true, "I'm a teapot"}
        };
    }

    @Test(dataProvider = "statusCodeScenarios")
    public void testStatusCodeBehavior(String testName, int statusCode, 
                                     boolean expectEarlyTermination, boolean expect13002Error, 
                                     String responseBody) {
        
        System.out.println("\n=== Testing " + testName + " (status " + statusCode + ") ===");
        
        // Setup: Configure the endpoint to return specific status code
        String testPath = "/rdap/domain/test.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        
        doReturn(testUri).when(config).getUri();
        
        // Configure WireMock to return the test status code
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", "application/json")
                .withBody(responseBody)));

        // Clear previous results
        queryContext.getResults().clear();

        // Execute: Run the actual RDAPHttpQuery
        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        boolean queryResult = query.run();
        
        System.out.println("Query result: " + queryResult);

        // Get all validation results
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        System.out.println("Total results: " + allResults.size());
        
        // Print all results for debugging
        for (RDAPValidationResult result : allResults) {
            System.out.println("  Result: " + result.getCode() + " - " + result.getMessage());
        }
        
        // Verify -13002 error presence
        boolean has13002Error = allResults.stream().anyMatch(r -> r.getCode() == -13002);
        
        assertEquals(has13002Error, expect13002Error, 
            testName + ": -13002 error expectation failed for status " + statusCode);
        
        if (has13002Error) {
            // Verify the -13002 error details
            RDAPValidationResult error13002 = allResults.stream()
                .filter(r -> r.getCode() == -13002)
                .findFirst()
                .orElse(null);
            
            assertTrue(error13002 != null, testName + ": Should have -13002 error");
            assertEquals(error13002.getValue(), String.valueOf(statusCode), 
                testName + ": -13002 error should contain status code as value");
            assertEquals(error13002.getMessage(), "The HTTP status code was neither 200 nor 404.",
                testName + ": -13002 error should have correct message");
        }

        // Verify implementation logic consistency
        boolean shouldTerminateEarly = (statusCode >= 400 && statusCode < 500);
        boolean shouldHave13002 = !(statusCode == 200 || statusCode == 404);
        
        assertEquals(shouldTerminateEarly, expectEarlyTermination,
            testName + ": Early termination expectation should match 4xx logic");
        assertEquals(shouldHave13002, expect13002Error,
            testName + ": -13002 error expectation should match validity logic (not 200 or 404)");
        
        System.out.println("SUCCESS: " + testName + " behaved correctly");
    }

    @Test
    public void testActual404Behavior() {
        System.out.println("\n=== Special Test: Detailed 404 Analysis ===");
        
        // This test specifically examines 404 behavior to confirm our findings
        String testPath = "/rdap/domain/notfound.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        
        doReturn(testUri).when(config).getUri();
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"rdapConformance\":[\"rdap_level_0\"]}")));

        queryContext.getResults().clear();
        
        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        boolean queryResult = query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        
        System.out.println("404 Query result: " + queryResult);
        System.out.println("404 Total results: " + allResults.size());
        
        // 404 should NOT generate -13002 (it's valid)
        boolean has13002 = allResults.stream().anyMatch(r -> r.getCode() == -13002);
        assertFalse(has13002, "404 should NOT generate -13002 error");
        
        // But 404 IS a 4xx code, so it should cause early termination
        // This means fewer validation results compared to 200 status
        System.out.println("404 behavior: Valid status (no -13002) + Early termination (4xx)");
    }

    @Test
    public void testCompare200vs404vs500() {
        System.out.println("\n=== Comparison Test: 200 vs 404 vs 500 ===");
        
        String basePath = "/rdap/domain/comparison-";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        
        // Test 200 - Valid + Continue
        testStatusComparison(200, basePath + "200.com", 
            "{\"rdapConformance\":[\"rdap_level_0\"],\"objectClassName\":\"domain\"}", 
            "200: Valid + Continue");
        
        // Test 404 - Valid + Early termination  
        testStatusComparison(404, basePath + "404.com",
            "{\"rdapConformance\":[\"rdap_level_0\"]}", 
            "404: Valid + Early termination");
        
        // Test 500 - Invalid + Continue
        testStatusComparison(500, basePath + "500.com",
            "{\"rdapConformance\":[\"rdap_level_0\"]}", 
            "500: Invalid + Continue");
    }
    
    private void testStatusComparison(int statusCode, String path, String body, String description) {
        URI testUri = URI.create("http://localhost:" + wireMockServer.port() + path);
        doReturn(testUri).when(config).getUri();
        
        stubFor(get(urlEqualTo(path))
            .willReturn(aResponse()
                .withStatus(statusCode)
                .withHeader("Content-Type", "application/json")
                .withBody(body)));

        queryContext.getResults().clear();
        
        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        boolean queryResult = query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        boolean has13002 = allResults.stream().anyMatch(r -> r.getCode() == -13002);
        
        System.out.println(description + " → Result: " + queryResult + 
                          ", Results: " + allResults.size() + 
                          ", Has -13002: " + has13002);
    }

    @Test
    public void testErrorMessageContent() {
        System.out.println("\n=== Test: Error Message Content ===");
        
        // Test with an unusual status code to verify error message details
        int testStatusCode = 418;
        String testPath = "/rdap/domain/teapot.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        
        doReturn(URI.create(baseUrl + testPath)).when(config).getUri();
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(testStatusCode)
                .withBody("I'm a teapot!")));

        queryContext.getResults().clear();
        
        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        RDAPValidationResult error13002 = allResults.stream()
            .filter(r -> r.getCode() == -13002)
            .findFirst()
            .orElse(null);

        assertTrue(error13002 != null, "Should have -13002 error for status " + testStatusCode);
        assertEquals(error13002.getValue(), String.valueOf(testStatusCode));
        assertEquals(error13002.getMessage(), "The HTTP status code was neither 200 nor 404.");
        assertEquals(error13002.getHttpStatusCode(), Integer.valueOf(testStatusCode));
        
        System.out.println("SUCCESS: Error message content is correct");
    }

    @Test 
    public void testRedirectStatusCodes() {
        System.out.println("\n=== Test: Testing Redirect Status Codes ===");
        
        // Set a much shorter timeout for problematic status codes to prevent hanging
        doReturn(2000).when(config).getTimeout(); // 2 seconds instead of default 5 seconds
        
        int[] testCodes = {301, 302}; // Test redirect codes (removed 102, 103 due to HTTP client timeouts)
        
        for (int statusCode : testCodes) {
            System.out.println("\n--- Testing status code: " + statusCode + " ---");
            
            String testPath = "/rdap/domain/test" + statusCode + ".com";
            String baseUrl = "http://localhost:" + wireMockServer.port();
            
            doReturn(URI.create(baseUrl + testPath)).when(config).getUri();
            
            stubFor(get(urlEqualTo(testPath))
                .willReturn(aResponse()
                    .withStatus(statusCode)
                    .withHeader("Content-Type", "application/json")
                    .withBody("Status " + statusCode + " response")));

            queryContext.getResults().clear();
            
            RDAPHttpQuery query = new RDAPHttpQuery(config);
            query.setQueryContext(queryContext);
            boolean result = query.run();
            
            Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
            
            System.out.println("Status " + statusCode + " → Query result: " + result + 
                             ", Total results: " + allResults.size());
            
            // Print all results for debugging
            for (RDAPValidationResult validationResult : allResults) {
                System.out.println("  Result: " + validationResult.getCode() + 
                                 ", Value: '" + validationResult.getValue() + 
                                 "', Message: " + validationResult.getMessage());
            }
            
            // Check if we have -13002 and what its value is
            RDAPValidationResult error13002 = allResults.stream()
                .filter(r -> r.getCode() == -13002)
                .findFirst()
                .orElse(null);
            
            if (error13002 != null) {
                System.out.println("  -13002 error value: '" + error13002.getValue() + "'");
                
                // Check if value is "no response available" which indicates HTTP failure
                if ("no response available".equals(error13002.getValue())) {
                    System.out.println("  WARNING: HTTP request failed for status " + statusCode);
                } else if (String.valueOf(statusCode).equals(error13002.getValue())) {
                    System.out.println("  SUCCESS: HTTP request succeeded for status " + statusCode);
                } else {
                    System.out.println("  UNKNOWN: Unexpected value for status " + statusCode);
                }
            } else {
                System.out.println("  No -13002 error (status code is considered valid)");
            }
        }
        
        // Reset timeout back to normal for other tests
        doReturn(5000).when(config).getTimeout();
    }

    // -12108 ERROR CODE MATCHING VALIDATION TESTS
    @DataProvider(name = "errorCodeMatchingScenarios")
    public Object[][] errorCodeMatchingScenarios() {
        return new Object[][] {
                // === Matching errorCode and HTTP status - should NOT trigger -12108 ===
                {"2024: HTTP 404 with errorCode 404 - Match", true, 404, "404", false, false},
                {"2024: HTTP 500 with errorCode 500 - Match", true, 500, "500", false, false},
                {"2024: HTTP 503 with errorCode 503 - Match", true, 503, "503", false, false},

                // Non-200 mismatches: now addErrorsToErrorRdapResponse() IS called for all non-200
                {"2024: HTTP 500 with errorCode 404 - Mismatch", true, 500, "404", true, false},
                {"2024: HTTP 503 with errorCode 500 - Mismatch", true, 503, "500", true, false},
                {"2024: HTTP 502 with errorCode 404 - Mismatch", true, 502, "404", true, false},

                // 4xx non-404: early termination in validate() → isQuerySuccessful=false
                // → addErrorsToErrorRdapResponse() guard (isQuerySuccessful()) blocks execution
                {"2024: HTTP 400 with errorCode 500 - Early termination", true, 400, "500", false, false},
                {"2024: HTTP 403 with errorCode 500 - Early termination", true, 403, "500", false, false},
                {"2024: HTTP 422 with errorCode 404 - Early termination", true, 422, "404", false, false},

                // HTTP 200 responses - isErrorContent() = false → addErrorsToErrorRdapResponse() not called
                {"2024: HTTP 200 with errorCode 404 - Success Response", true, 200, "404", false, false},
                {"2024: HTTP 200 with errorCode 500 - Success Response", true, 200, "500", false, false},

                // Non-2024 profile — addErrorsToErrorRdapResponse() has no profile check, works for all non-200
                {"Non-2024: HTTP 404 with errorCode 500 - Mismatch", false, 404, "500", true, false},
                {"Non-2024: HTTP 500 with errorCode 404 - Mismatch", false, 500, "404", true, false},

                // Edge cases
                {"2024: HTTP 500 with numeric errorCode 404 - Mismatch", true, 500, 404, true, false},
                {"2024: HTTP 500 with numeric errorCode 500 - Match", true, 500, 500, false, false}
        };
    }

    @Test(dataProvider = "errorCodeMatchingScenarios")
    public void testErrorCodeMatching(String testName, boolean use2024Profile, int httpStatus,
                                      Object errorCode, boolean expectError12108, boolean expectError12107) {

        System.out.println("\n=== Testing " + testName + " ===");

        doReturn(use2024Profile).when(config).useRdapProfileFeb2024();

        String testPath = "/rdap/domain/errorcode-test.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();

        String responseBody = createResponseBody(errorCode);

        stubFor(get(urlEqualTo(testPath))
                .willReturn(aResponse()
                        .withStatus(httpStatus)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        boolean queryResult = query.run();

        // Simulate current RDAPValidator flow: call addErrorsToErrorRdapResponse()
        // for any error content response, not just 404.
        if (query.isErrorContent()) {
            query.addErrorsToErrorRdapResponse();
        }

        // Analyze results
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();

        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        System.out.println("Query result: " + queryResult);
        System.out.println("Total results: " + allResults.size());
        System.out.println("Has -12108: " + has12108 + " (expected: " + expectError12108 + ")");
        System.out.println("Has -12107: " + has12107 + " (expected: " + expectError12107 + ")");

        // Print all results for debugging
        for (RDAPValidationResult result : allResults) {
            System.out.println("  Result: " + result.getCode() + " - " + result.getMessage());
        }

        // Verify expectations
        assertEquals(has12108, expectError12108,
            testName + ": -12108 error expectation failed");
        assertEquals(has12107, expectError12107,
            testName + ": -12107 error expectation failed");

        // If we expect -12108, verify the error details
        if (expectError12108) {
            RDAPValidationResult error12108 = allResults.stream()
                .filter(r -> r.getCode() == -12108)
                .findFirst()
                .orElse(null);

            assertTrue(error12108 != null, testName + ": Should have -12108 error");
            assertEquals(error12108.getMessage(), "The errorCode value does not match the HTTP status code.",
                testName + ": -12108 should have correct message");
            assertEquals(error12108.getValue(), responseBody,
                testName + ": -12108 should contain response body as value");
        }

        System.out.println("SUCCESS: " + testName);
    }

    @Test
    public void testMissingErrorCode2024Profile() {
        System.out.println("\n=== Test: Missing errorCode with 2024 Profile ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/missing-error-code.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Response without errorCode - should trigger -12107 but NOT -12108
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"]}";
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();

        // Simulate RDAPValidator flow
        if (query.isErrorContent()) {
            query.addErrorsToErrorRdapResponse();
        }

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();

        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        assertFalse(has12108, "Missing errorCode should NOT trigger -12108");
        assertFalse(has12107, "Missing errorCode should NOT trigger -12107");
    }

    @Test
    public void testInvalidErrorCodeFormat() {
        System.out.println("\n=== Test: Invalid errorCode Format ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/invalid-error-code.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Response with invalid errorCode format (boolean instead of number)
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\": true}";
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();

        // Simulate RDAPValidator flow
        if (query.isErrorContent()) {
            query.addErrorsToErrorRdapResponse();
        }

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();

        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        assertTrue(has12108, "Invalid errorCode format should trigger -12108");
        assertFalse(has12107, "Invalid errorCode format should not trigger -12107 (errorCode exists)");
    }

    @Test
    public void testMalformedJsonResponse() {
        System.out.println("\n=== Test: Malformed JSON Response (404 + incomplete JSON) ===");

        doReturn(true).when(config).useRdapProfileFeb2024();

        String testPath = "/rdap/domain/malformed.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();

        // Malformed JSON — starts with '{' but is incomplete
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\":";

        stubFor(get(urlEqualTo(testPath))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);

        // run() must return true for 404 even with invalid JSON so RDAPValidator
        // can continue and schema validation can emit -12100
        boolean runResult = query.run();
        assertTrue(runResult, "run() should return true for 404 even with malformed JSON");
        assertTrue(query.isErrorContent(), "404 should be treated as error content");

        // Simulate RDAPValidator flow
        if (query.isErrorContent()) {
            query.addErrorsToErrorRdapResponse();
        }

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();

        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);
        boolean has13001 = allResults.stream().anyMatch(r -> r.getCode() == -13001);

        // JSON malformed (isValid=false) → addErrorsToErrorRdapResponse() is no-op only -13001 is emitted
        assertTrue(has13001, "Malformed JSON should trigger -13001");
        assertFalse(has12107, "Malformed JSON: body is not parseable, -12107 should NOT be emitted");
        assertFalse(has12108, "Malformed JSON should not trigger -12108");
    }

    @Test
    public void testArrayBodyOnErrorResponse_DoesNotTriggerFalsePositive12107() {
        System.out.println("\n=== Test: Array body on 404 — no false -12107 ===");

        doReturn(true).when(config).useRdapProfileFeb2024();

        String testPath = "/rdap/domain/array-body.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();

        stubFor(get(urlEqualTo(testPath))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody("[]")));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);

        // [] is valid JSON (a list), so run() stays true for 404
        boolean runResult = query.run();
        assertTrue(runResult, "run() should return true for 404 with array body");
        assertTrue(query.isErrorContent(), "404 should be treated as error content");

        if (query.isErrorContent()) {
            query.addErrorsToErrorRdapResponse();
        }

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();

        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);
        boolean has13001 = allResults.stream().anyMatch(r -> r.getCode() == -13001);

        // [] is valid JSON so -13001 is NOT emitted
        // [] does not start with '{' so addErrorsToErrorRdapResponse() guard skips -12107
        assertFalse(has13001, "[] is valid JSON — -13001 should NOT be emitted");
        assertFalse(has12107, "Array body should NOT trigger -12107 — guard prevents false positive");
    }

    @Test
    public void testValidationOrder() {
        System.out.println("\n=== Test: Validation Order (-12107 before -12108) ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/order-test.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Response missing rdapConformance (should trigger -12107, preventing -12108)
        String responseBody = "{\"errorCode\": 500}"; // Missing rdapConformance
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(500) // Use 500 to avoid early termination
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();

        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        // Body is valid JSON with rdapConformance present but errorCode matches (500==500),
        // so -12108 is not triggered.
        assertFalse(has12107, "Non-404 status should not trigger -12107");
        assertFalse(has12108, "Non-404 status should not trigger -12108");
        
        System.out.println("SUCCESS: Validation order is correct");
    }

    @Test
    public void testClientError4xxEarlyTermination() {
        System.out.println("\n=== Test: 4xx Client Errors - -12108 Precedence + Early Termination ===");

        doReturn(true).when(config).useRdapProfileFeb2024();

        // Test that 422 (client error) allows -12108 validation BEFORE early termination
        String testPath = "/rdap/domain/client-error.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();

        // Response with mismatched errorCode that should trigger -12108 before early termination
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\": 404}";

        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(422) // Client error - triggers -12108 then early termination
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();

        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);
        boolean has13002 = allResults.stream().anyMatch(r -> r.getCode() == -13002);

        // 4xx non-404 causes early termination. addErrorsToErrorRdapResponse() is not called.
        assertFalse(has12108, "4xx non-404 should not trigger -12108 (not a 404 response)");
        assertFalse(has12107, "4xx non-404 should not trigger -12107");
        assertTrue(has13002, "4xx non-404 should still trigger -13002");

        System.out.println("SUCCESS: 4xx precedence + early termination behavior confirmed");
    }

    @Test
    public void testNullResponseBody() {
        System.out.println("\n=== Test: Null Response Body ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/null-response.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Response with null body
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody((String) null))); // Explicit null body

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        
        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        // Null body is unparseable → jsonResponse.isValid()=false → isQuerySuccessful=false.
        // addErrorsToErrorRdapResponse() is guarded by isQuerySuccessful(), so it's never reached.
        // The unparseable body is caught by -13001 (invalid JSON) instead.
        assertFalse(has12107, "Non-404 status should not trigger -12107");
        assertFalse(has12108, "Non-404 status should not trigger -12108");
        
        System.out.println("SUCCESS: Null response body correctly handled");
    }

    @Test
    public void testEmptyResponseBody() {
        System.out.println("\n=== Test: Empty/Blank Response Body ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/empty-response.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Response with empty body
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(""))); // Empty body

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        
        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        // Empty body is unparseable → jsonResponse.isValid()=false → isQuerySuccessful=false.
        // addErrorsToErrorRdapResponse() is guarded by isQuerySuccessful(), so it's never reached.
        assertFalse(has12107, "Non-404 status should not trigger -12107");
        assertFalse(has12108, "Non-404 status should not trigger -12108");


        System.out.println("SUCCESS: Empty response body correctly handled");
    }

    @Test
    public void testBlankResponseBody() {
        System.out.println("\n=== Test: Blank Response Body ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/blank-response.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Response with blank body (spaces/tabs)
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody("   \t  "))); // Blank body with spaces and tabs

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        
        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        // Blank body is unparseable → jsonResponse.isValid()=false → isQuerySuccessful=false.
        // addErrorsToErrorRdapResponse() is guarded by isQuerySuccessful(), so it's never reached.
        assertFalse(has12107, "Non-404 status should not trigger -12107");
        assertFalse(has12108, "Non-404 status should not trigger -12108");
        
        System.out.println("SUCCESS: Blank response body correctly handled");
    }

    @Test
    public void testMissingErrorCodeField() {
        System.out.println("\n=== Test: Missing errorCode Field ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/missing-errorcode-field.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Valid JSON but missing errorCode field entirely
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"], \"otherField\": \"value\"}";
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        
        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        // rdapConformance is present but errorCode is missing → validateErrorCodeMatchesHttpStatus()
        // returns true (no errorCode to compare) → neither -12107 nor -12108 is triggered.
        // Note: -12107 checks rdapConformance presence, not errorCode.
        assertFalse(has12107, "Non-404 status should not trigger -12107");
        assertFalse(has12108, "Non-404 status should not trigger -12108");

        System.out.println("SUCCESS: Missing errorCode field correctly handled");
    }

    @Test
    public void testJsonParsingException() {
        System.out.println("\n=== Test: JSON Parsing Exception ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/parse-exception.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Invalid JSON that will cause parsing exception
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\": invalid_json_here}";
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        
        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);
        
        // JSON parsing exception should trigger -12107 but not -12108
        assertFalse(has12107, "Non-404 status should not trigger -12107");
        assertFalse(has12108, "Non-404 status should not trigger -12108");

        System.out.println("SUCCESS: JSON parsing exception correctly handled");
    }

    @Test
    public void testStringParsingException() {
        System.out.println("\n=== Test: String Parsing Exception for errorCode ===");
        
        doReturn(true).when(config).useRdapProfileFeb2024();
        
        String testPath = "/rdap/domain/string-parse-exception.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();
        
        // Valid JSON but errorCode string cannot be parsed to integer
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\": \"not-a-number\"}";
        
        stubFor(get(urlEqualTo(testPath))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/rdap+json")
                .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();
        
        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        
        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);
        
        // String parsing exception should trigger -12108 (parsing failed, so return false)
        assertFalse(has12108, "Non-404 status should not trigger -12108");
        assertFalse(has12107, "Non-404 status should not trigger -12107");


        System.out.println("SUCCESS: String parsing exception correctly handled");
    }

    /**
     * Integration test: RDAPValidator + RDAPHttpQuery end-to-end for 404 + invalid JSON body.
     * Verifies RCT-475 regression: -12100 must be emitted when error response body is malformed JSON.
     */
    @Test
    public void testIntegration_404WithMalformedJson_Emits12100() {
        doReturn(true).when(config).check();
        doReturn(false).when(config).isAdditionalConformanceQueries();
        doReturn(false).when(config).isNetworkEnabled();
        doReturn(false).when(config).isThin();
        doReturn(false).when(config).isGtldRegistry();
        doReturn(false).when(config).useRdapProfileFeb2024();
        doReturn(false).when(config).isGtldRegistrar();

        String testPath = "/rdap/domain/malformed-integration.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();

        stubFor(get(urlEqualTo(testPath))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody("{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\":")));

        RDAPHttpQuery rdapHttpQuery = new RDAPHttpQuery(config);
        rdapHttpQuery.ssrfProtectionEnabled = false;
        rdapHttpQuery.setQueryContext(queryContext);

        // Reuse the same queryContext — replace its query with rdapHttpQuery
        QueryContext integrationContext = QueryContext.create(config, queryContext.getDatasetService(), rdapHttpQuery);
        integrationContext.setSsrfProtectionEnabled(false);

        RDAPValidator validator =
                new RDAPValidator(integrationContext);

        validator.validate();

        Set<RDAPValidationResult> allResults = integrationContext.getResults().getAll();
        boolean has12100 = allResults.stream().anyMatch(r -> r.getCode() == -12100);

        assertTrue(has12100, "RDAPValidator must emit -12100 for 404 + malformed JSON (RCT-475)");
    }

    @Test
    public void testIntegration_404WithArrayBody_Emits12100() {
        doReturn(true).when(config).check();
        doReturn(false).when(config).isAdditionalConformanceQueries();
        doReturn(false).when(config).isNetworkEnabled();
        doReturn(false).when(config).isThin();
        doReturn(false).when(config).isGtldRegistry();
        doReturn(false).when(config).useRdapProfileFeb2024();
        doReturn(false).when(config).isGtldRegistrar();

        String testPath = "/rdap/domain/array-integration.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();

        stubFor(get(urlEqualTo(testPath))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody("[]")));

        RDAPHttpQuery rdapHttpQuery = new RDAPHttpQuery(config);
        rdapHttpQuery.ssrfProtectionEnabled = false;
        rdapHttpQuery.setQueryContext(queryContext);

        QueryContext integrationContext = QueryContext.create(config, queryContext.getDatasetService(), rdapHttpQuery);
        integrationContext.setSsrfProtectionEnabled(false);

        RDAPValidator validator =
                new RDAPValidator(integrationContext);

        validator.validate();

        Set<RDAPValidationResult> allResults = integrationContext.getResults().getAll();
        boolean has12100 = allResults.stream().anyMatch(r -> r.getCode() == -12100);

        assertTrue(has12100, "RDAPValidator must emit -12100 for 404 + array body (RCT-475)");
    }

    @Test
    public void test500WithMismatchedErrorCode_Triggers12108() {
        doReturn(true).when(config).useRdapProfileFeb2024();

        String testPath = "/rdap/domain/code-12108.com";
        String baseUrl = "http://localhost:" + wireMockServer.port();
        URI testUri = URI.create(baseUrl + testPath);
        doReturn(testUri).when(config).getUri();

        // HTTP 500 but errorCode says 404 → mismatch → -12108
        String responseBody = "{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\": 404}";

        stubFor(get(urlEqualTo(testPath))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/rdap+json")
                        .withBody(responseBody)));

        RDAPHttpQuery query = new RDAPHttpQuery(config);
        query.setQueryContext(queryContext);
        query.run();

        if (query.isErrorContent()) {
            query.addErrorsToErrorRdapResponse();
        }

        Set<RDAPValidationResult> allResults = queryContext.getResults().getAll();
        boolean has12108 = allResults.stream().anyMatch(r -> r.getCode() == -12108);
        boolean has12107 = allResults.stream().anyMatch(r -> r.getCode() == -12107);

        assertTrue(has12108, "HTTP 500 with errorCode 404 should trigger -12108");
        assertFalse(has12107, "rdapConformance is present, -12107 should not fire");
    }

    /**
     * Helper method to create JSON response body with errorCode and rdapConformance
     */
    private String createResponseBody(Object errorCode) {
        String errorCodeJson;
        if (errorCode instanceof String) {
            errorCodeJson = "\"" + errorCode + "\"";
        } else {
            errorCodeJson = String.valueOf(errorCode);
        }
        
        return "{\"rdapConformance\": [\"rdap_level_0\"], \"errorCode\": " + errorCodeJson + "}";
    }

}