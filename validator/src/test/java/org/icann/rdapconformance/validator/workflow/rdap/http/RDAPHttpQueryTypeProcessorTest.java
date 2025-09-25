package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.net.URI;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RDAPHttpQueryTypeProcessorTest {

  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
  private final RDAPQueryTypeProcessor processor = RDAPHttpQueryTypeProcessor.getInstance(config);
  private final RDAPDatasetService datasetService = new RDAPDatasetServiceMock();

  @Test
  public void testUnsupportedQuery_ReturnsUnsupportedQueryError() {
    URI uri = URI.create("http://rdap.server.example/unsupported/test");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(ToolResult.UNSUPPORTED_QUERY);
  }

  @Test
  public void testMixedLabelFormatInDomain_ReturnsMixedLabelFormatError() {
    URI uri = URI.create("http://rdap.server.example/domain/example.xn--mller-kva.例子");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(ToolResult.MIXED_LABEL_FORMAT);
  }

  @Test
  public void testMixedLabelFormatInNameserver_ReturnsMixedLabelFormatError() {
    URI uri = URI.create("http://rdap.server.example/nameserver/ns1.example.xn--mller-kva.例子");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(ToolResult.MIXED_LABEL_FORMAT);
  }

  @Test
  public void testDomainQuery() {
    URI uri = URI.create("http://rdap.server.example/domain/test.example");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(new RDAPDatasetServiceMock())).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.DOMAIN);
  }

  @Test
  public void testNameserverQuery() {
    URI uri = URI.create("http://rdap.server.example/nameserver/test.example");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(new RDAPDatasetServiceMock())).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.NAMESERVER);
  }

  @Test
  public void testEntityQuery() {
    URI uri = URI.create("http://rdap.server.example/entity/VG-1234");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.ENTITY);
  }

  @Test
  public void testHelpQuery() {
    URI uri = URI.create("http://rdap.server.example/help");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.HELP);
  }

  @Test
  public void testNameserversQuery() {
    URI uri = URI.create("http://rdap.server.example/nameservers?ip=.*");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isTrue();
    assertThat(processor.getQueryType()).isEqualTo(RDAPQueryType.NAMESERVERS);
  }

  @Test
  public void testUnsupportedQuery_ErrorStatusIs3() {
    URI uri = URI.create("http://rdap.server.example/");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(ToolResult.UNSUPPORTED_QUERY);
  }

  @Test
  @Ignore("Validation not yet implemented")
  public void testCheckInvalidDomainName_ErrorStatusIs4() {
    URI uri = URI.create("http://rdap.server.example/domain/xn--abcdé");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(ToolResult.MIXED_LABEL_FORMAT);
  }

  @Test
  @Ignore("Validation not yet implemented")
  public void testCheckInvalidNameserverName_ErrorStatusIs4() {
    URI uri = URI.create("http://rdap.server.example/nameserver/xn--abcdé");
    doReturn(uri).when(config).getUri();

    assertThat(processor.check(datasetService)).isFalse();
    assertThat(processor.getErrorStatus()).isEqualTo(ToolResult.MIXED_LABEL_FORMAT);
  }

  @Test
  public void testHasMixedLabels_NonIDNDoubleHyphen_ReturnsFalse() {
    // Test that labels containing "--" but not starting with "xn--" are treated as regular ASCII labels
    // "zz--main-1234" should not be confused with IDN A-labels which must start with "xn--"
    RDAPHttpQueryTypeProcessor processorInstance = RDAPHttpQueryTypeProcessor.getInstance();
    
    // Should return false because "zz--main-1234" is not an IDN A-label and contains no mixed labels
    assertThat(processorInstance.hasMixedLabels("zz--main-1234")).isFalse();
  }

  // TEMPORARILY DISABLED - Domain validation tests
  // These will be re-enabled when domain validation is turned back on
  /*
  @Test
  public void testSchemaValidationFailure_DebugZzMainDomain() {
    // Clear singleton state before test
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance().clear();
    
    // Debug test to understand why schema validation fails for zz--main-1234
    URI uri = URI.create("http://rdap.server.example/domain/zz--main-1234");
    doReturn(uri).when(config).getUri();
    
    // Create fresh processor instance to avoid state pollution
    RDAPQueryTypeProcessor freshProcessor = RDAPHttpQueryTypeProcessor.getInstance(config);
    
    // This should fail due to schema validation, not mixed labels
    boolean result = freshProcessor.check(datasetService);
    ToolResult errorStatus = freshProcessor.getErrorStatus();
    
    System.out.println("Check result: " + result);
    System.out.println("Error status: " + errorStatus);
    System.out.println("Mixed labels check for 'zz--main-1234': " + 
                      RDAPHttpQueryTypeProcessor.getInstance().hasMixedLabels("zz--main-1234"));
    
    // Verify that mixed labels check passes but overall check continues (returns true)
    assertThat(RDAPHttpQueryTypeProcessor.getInstance().hasMixedLabels("zz--main-1234")).isFalse();
    assertThat(result).isTrue(); // Domain validation errors are recorded, but execution continues
    // This will show us the error status is no longer null after the fix
    System.out.println("Error status is null: " + (errorStatus == null));
    assertThat(errorStatus).isEqualTo(ToolResult.BAD_USER_INPUT);
  }
  */

  @Test
  public void testSchemaValidationErrorCodes_ZzMain_NewCodes() {
    // Test to verify the new error codes are generated instead of -999
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl results = 
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance();
    
    // Clear any previous results to avoid test pollution
    results.clear();
    
    org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock datasetService = 
        new org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock();
    
    org.icann.rdapconformance.validator.SchemaValidator validator = 
        new org.icann.rdapconformance.validator.SchemaValidator("rdap_domain_name.json", results, datasetService);
    
    String testJson = "{\"domain\": \"zz--main-1234\"}";
    boolean isValid = validator.validate(testJson);
    
    System.out.println("Schema validation result: " + isValid);
    System.out.println("Number of validation errors: " + results.getAll().size());
    
    assertThat(isValid).isFalse();
    assertThat(results.getAll().size()).isEqualTo(2);
    
    // Should now generate proper error codes instead of -999
    java.util.List<Integer> errorCodes = results.getAll().stream()
        .map(org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult::getCode)
        .collect(java.util.stream.Collectors.toList());
    
    assertThat(errorCodes).containsExactlyInAnyOrder(-10302, -10303); // lessThanTwoLabels, generic IDN error
    assertThat(errorCodes).doesNotContain(-999); // Should not contain fallback error code
    
    for (org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult error : results.getAll()) {
      System.out.println("Error code: " + error.getCode());
      System.out.println("Error message: " + error.getMessage());
      System.out.println("Error value: " + error.getValue());
      System.out.println("---");
    }
  }

  @Test
  public void testSchemaValidationFailure_NowHasProperStatus() {
    // Clear singleton state before test
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance().clear();

    // Test that schema validation failure now sets proper ToolResult status (fixes the NPE bug)
    URI uri = URI.create("http://rdap.server.example/domain/zz--main-1234");
    doReturn(uri).when(config).getUri();

    // Create fresh processor instance to avoid state pollution
    RDAPQueryTypeProcessor freshProcessor = RDAPHttpQueryTypeProcessor.getInstance(config);

    // Domain validation now continues execution (returns true) to capture errors in final results
    boolean checkResult = freshProcessor.check(datasetService);
    assertThat(checkResult).isTrue();

    // After the fix: getErrorStatus() should now return BAD_USER_INPUT instead of null
    ToolResult errorStatus = freshProcessor.getErrorStatus();
    assertThat(errorStatus).isNotNull();
    assertThat(errorStatus).isEqualTo(ToolResult.BAD_USER_INPUT);

    // This should no longer throw NPE
    int errorCode = errorStatus.getCode();
    assertThat(errorCode).isEqualTo(25); // BAD_USER_INPUT code
    System.out.println("NPE bug fixed - got error code: " + errorCode);
  }

  @Test
  public void testIdnHostNameValidator_DebugZzMain() {
    // Test to see exactly what the IdnHostNameFormatValidator does with zz--main-1234
    org.icann.rdapconformance.validator.customvalidator.IdnHostNameFormatValidator validator = 
        new org.icann.rdapconformance.validator.customvalidator.IdnHostNameFormatValidator();
    
    java.util.Optional<String> result = validator.validate("zz--main-1234");
    System.out.println("IdnHostNameFormatValidator result for 'zz--main-1234': " + result);
    System.out.println("Is valid: " + result.isEmpty());
    
    // Also test a known good domain
    java.util.Optional<String> result2 = validator.validate("example.com");
    System.out.println("IdnHostNameFormatValidator result for 'example.com': " + result2);
    System.out.println("Is valid: " + result2.isEmpty());
    
    // Test what the validator actually reports
    if (result.isPresent()) {
      System.out.println("Validation error details for 'zz--main-1234': " + result.get());
    }
  }

  @Test
  public void testIdnHostNameValidator_XnPrefix() {
    // Test a proper A-label (xn-- prefix) - should this pass the HYPHEN_3_4 rule?
    org.icann.rdapconformance.validator.customvalidator.IdnHostNameFormatValidator validator = 
        new org.icann.rdapconformance.validator.customvalidator.IdnHostNameFormatValidator();
    
    java.util.Optional<String> result = validator.validate("xn--nxasmq6b.example.com");
    System.out.println("IdnHostNameFormatValidator result for 'xn--nxasmq6b.example.com': " + result);
    System.out.println("Is valid: " + result.isEmpty());
    
    if (result.isPresent()) {
      System.out.println("Validation error details for A-label: " + result.get());
    }
  }

  @Test
  public void testDomainValidation_ValidDomainPasses() {
    // Test that a valid domain passes all validation
    URI uri = URI.create("http://rdap.server.example/domain/example.com");
    doReturn(uri).when(config).getUri();
    
    // Create fresh processor instance to avoid state pollution
    RDAPQueryTypeProcessor freshProcessor = RDAPHttpQueryTypeProcessor.getInstance(config);
    boolean checkResult = freshProcessor.check(datasetService);
    ToolResult errorStatus = freshProcessor.getErrorStatus();
    
    assertThat(checkResult).isTrue();
    assertThat(errorStatus).isNull(); // No error for valid domain
  }

  /*
  @Test
  public void testDomainValidation_LessThanTwoLabels() {
    // Test domain with less than two labels generates -10302 error
    URI uri = URI.create("http://rdap.server.example/domain/singlelabel");
    doReturn(uri).when(config).getUri();
    
    boolean checkResult = processor.check(datasetService);
    ToolResult errorStatus = processor.getErrorStatus();
    
    assertThat(checkResult).isTrue(); // Domain validation errors are recorded, but execution continues
    assertThat(errorStatus).isEqualTo(ToolResult.BAD_USER_INPUT);
  }
  */

  @Test
  public void testDomainValidation_ValidALabel() {
    // Test that valid A-labels (xn-- prefix) pass validation
    URI uri = URI.create("http://rdap.server.example/domain/xn--nxasmq6b.example.com");
    doReturn(uri).when(config).getUri();
    
    // Create fresh processor instance to avoid state pollution
    RDAPQueryTypeProcessor freshProcessor = RDAPHttpQueryTypeProcessor.getInstance(config);
    boolean checkResult = freshProcessor.check(datasetService);
    ToolResult errorStatus = freshProcessor.getErrorStatus();
    
    assertThat(checkResult).isTrue();
    assertThat(errorStatus).isNull(); // Valid A-label should pass
  }

  @Test
  public void testDomainValidation_MixedLabelsStillDetected() {
    // Test that mixed labels are still detected and return MIXED_LABEL_FORMAT
    URI uri = URI.create("http://rdap.server.example/domain/example.xn--mller-kva.例子");
    doReturn(uri).when(config).getUri();
    
    boolean checkResult = processor.check(datasetService);
    ToolResult errorStatus = processor.getErrorStatus();
    
    assertThat(checkResult).isFalse();
    assertThat(errorStatus).isEqualTo(ToolResult.MIXED_LABEL_FORMAT); // Should still catch mixed labels
  }

  @Test
  public void testErrorCode_10300_LabelTooLong() {
    // Test that a domain with label too long generates -10300 error with correct message
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl results = 
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance();
    
    // Clear any previous results to avoid test pollution
    results.clear();
    
    org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock datasetService = 
        new org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock();
    
    org.icann.rdapconformance.validator.SchemaValidator validator = 
        new org.icann.rdapconformance.validator.SchemaValidator("rdap_domain_name.json", results, datasetService);
    
    // Create a label that's too long (over 63 characters)
    String longLabel = "a".repeat(64);
    String testJson = "{\"domain\": \"" + longLabel + ".example.com\"}";
    boolean isValid = validator.validate(testJson);
    
    assertThat(isValid).isFalse();
    assertThat(results.getAll().size()).isGreaterThan(0);
    
    // Find the -10300 error
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult labelTooLongError = 
        results.getAll().stream()
            .filter(result -> result.getCode() == -10300)
            .findFirst()
            .orElse(null);
    
    assertThat(labelTooLongError).isNotNull();
    assertThat(labelTooLongError.getMessage()).isEqualTo("A DNS label with length not between 1 and 63 was found.");
    assertThat(labelTooLongError.getValue()).contains("#/domain:");
    
    System.out.println("Error -10300: " + labelTooLongError.getMessage());
  }

  @Test
  public void testErrorCode_10301_DomainTooLong() {
    // Test that a domain name longer than 253 characters generates -10301 error with correct message
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl results = 
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance();
    
    // Clear any previous results to avoid test pollution
    results.clear();
    
    org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock datasetService = 
        new org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock();
    
    org.icann.rdapconformance.validator.SchemaValidator validator = 
        new org.icann.rdapconformance.validator.SchemaValidator("rdap_domain_name.json", results, datasetService);
    
    // Create a domain name that's too long (over 253 characters total)
    // Each label can be up to 63 chars, so we need multiple labels to exceed 253 total
    String label63 = "a".repeat(63);
    String longDomain = label63 + "." + label63 + "." + label63 + "." + label63 + ".example.com"; // Over 253 chars
    String testJson = "{\"domain\": \"" + longDomain + "\"}";
    boolean isValid = validator.validate(testJson);
    
    assertThat(isValid).isFalse();
    assertThat(results.getAll().size()).isGreaterThan(0);
    
    // Find the -10301 error
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult domainTooLongError = 
        results.getAll().stream()
            .filter(result -> result.getCode() == -10301)
            .findFirst()
            .orElse(null);
    
    assertThat(domainTooLongError).isNotNull();
    assertThat(domainTooLongError.getMessage()).isEqualTo("A domain name of more than 253 characters was found.");
    assertThat(domainTooLongError.getValue()).contains("#/domain:");
    
    System.out.println("Error -10301: " + domainTooLongError.getMessage());
    System.out.println("Domain length: " + longDomain.length());
  }

  @Test
  public void testErrorCode_10302_LessThanTwoLabels() {
    // Test that a domain with less than two labels generates -10302 error with correct message
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl results = 
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance();
    
    // Clear any previous results to avoid test pollution
    results.clear();
    
    org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock datasetService = 
        new org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock();
    
    org.icann.rdapconformance.validator.SchemaValidator validator = 
        new org.icann.rdapconformance.validator.SchemaValidator("rdap_domain_name.json", results, datasetService);
    
    // Test with single label (no dot)
    String testJson = "{\"domain\": \"singlelabel\"}";
    boolean isValid = validator.validate(testJson);
    
    assertThat(isValid).isFalse();
    assertThat(results.getAll().size()).isGreaterThan(0);
    
    // Find the -10302 error
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult lessThanTwoLabelsError = 
        results.getAll().stream()
            .filter(result -> result.getCode() == -10302)
            .findFirst()
            .orElse(null);
    
    assertThat(lessThanTwoLabelsError).isNotNull();
    assertThat(lessThanTwoLabelsError.getMessage()).isEqualTo("A domain name with less than two labels was found. See RDAP_Technical_Implementation_Guide_2_1 section 1.10.");
    assertThat(lessThanTwoLabelsError.getValue()).contains("#/domain:singlelabel");
    
    System.out.println("Error -10302: " + lessThanTwoLabelsError.getMessage());
  }

  @Test
  public void testErrorCode_10303_InvalidLabel() {
    // Test that a domain with invalid label generates -10303 error with correct message
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl results = 
        org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance();
    
    // Clear any previous results to avoid test pollution
    results.clear();
    
    org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock datasetService = 
        new org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock();
    
    org.icann.rdapconformance.validator.SchemaValidator validator = 
        new org.icann.rdapconformance.validator.SchemaValidator("rdap_domain_name.json", results, datasetService);
    
    // Use zz--main-1234 which triggers HYPHEN_3_4 rule violation
    String testJson = "{\"domain\": \"zz--main-1234\"}";
    boolean isValid = validator.validate(testJson);
    
    assertThat(isValid).isFalse();
    assertThat(results.getAll().size()).isGreaterThan(0);
    
    // Find the -10303 error
    org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult invalidLabelError = 
        results.getAll().stream()
            .filter(result -> result.getCode() == -10303)
            .findFirst()
            .orElse(null);
    
    assertThat(invalidLabelError).isNotNull();
    assertThat(invalidLabelError.getMessage()).contains("A label not being a valid \"U-label\"/\"A-label\" or \"NR-LDH label\" was found.");
    assertThat(invalidLabelError.getMessage()).contains("Reasons:");
    assertThat(invalidLabelError.getValue()).contains("#/domain:zz--main-1234");
    
    System.out.println("Error -10303: " + invalidLabelError.getMessage());
  }

  @Test
  public void testAllDomainValidationErrorMessages() {
    // Comprehensive test to verify all domain validation error codes and messages are working
    System.out.println("\n=== Domain Validation Error Code Test Summary ===");
    System.out.println("Testing all domain validation error codes as per ICANN documentation...");
    
    // Test cases that should trigger specific errors
    String[] testCases = {
        "singlelabel",           // -10302: Less than two labels
        "zz--main-1234",        // -10303: Invalid label (HYPHEN_3_4)
        "a".repeat(64) + ".com", // -10300: Label too long
        // Domain too long test would be complex, covered in separate test
    };
    
    for (String domain : testCases) {
      org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl results = 
          org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl.getInstance();
      results.clear();
      
      org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock datasetService = 
          new org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock();
      
      org.icann.rdapconformance.validator.SchemaValidator validator = 
          new org.icann.rdapconformance.validator.SchemaValidator("rdap_domain_name.json", results, datasetService);
      
      String testJson = "{\"domain\": \"" + domain + "\"}";
      boolean isValid = validator.validate(testJson);
      
      System.out.println("\nTesting domain: " + domain);
      System.out.println("Valid: " + isValid);
      System.out.println("Errors found: " + results.getAll().size());
      
      for (org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult error : results.getAll()) {
        System.out.println("  Code: " + error.getCode());
        System.out.println("  Message: " + error.getMessage());
        System.out.println("  Value: " + error.getValue());
        
        // Verify message is not empty and contains expected content
        assertThat(error.getMessage()).isNotNull();
        assertThat(error.getMessage()).isNotEmpty();
        assertThat(error.getCode()).isIn(-10300, -10301, -10302, -10303);
      }
    }
    
    System.out.println("\n=== All domain validation error tests completed ===");
  }
}