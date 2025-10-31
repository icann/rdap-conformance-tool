package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ResponseValidationDomainInvalid_2024Test {

  private RDAPValidatorConfiguration mockConfig;
  private RDAPValidatorResults results;
  private ResponseValidationDomainInvalid_2024 responseValidator;
  private MockedStatic<RDAPHttpRequest> mockStaticRequest;
  private QueryContext queryContext;

  @BeforeMethod
  public void setup() {
    mockConfig = mock(RDAPValidatorConfiguration.class);
    queryContext = QueryContext.forTesting(mockConfig);
    results = queryContext.getResults();
    results.clear();
    responseValidator = new ResponseValidationDomainInvalid_2024(queryContext);

    // Initialize static mock for each test
    mockStaticRequest = mockStatic(RDAPHttpRequest.class);
  }

  @AfterMethod
  public void tearDown() {
    if (mockStaticRequest != null) {
      mockStaticRequest.close();
      mockStaticRequest = null;
    }
  }

  @Test
  public void testGetGroupName() {
    assertEquals(responseValidator.getGroupName(), "rdapResponseDomainInvalid_2024_Validation");
  }

  @Test
  public void testDoValidate_InvalidDomainTypeUrl_WithInvalidDomainInUri() throws Exception {
    URI uri = new URI("http://example.com/rdap");
    when(mockConfig.getUri()).thenReturn(uri);
    when(mockConfig.getTimeout()).thenReturn(1000);

    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{\"rdapConformance\":[], \"errorCode\":404}");
    when(mockResponse.uri()).thenReturn(URI.create("http://example.com/domain/test.invalid"));

    mockStaticRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(mockResponse);

    boolean result = responseValidator.doValidate();
    assertTrue(result);
  }

  @Test
  public void testDoValidate_InvalidDomainTypeUrl_WithInvalidInUri_AndQueryType() throws Exception {
    URI uri = new URI("http://example.com/rdap");
    MockedStatic<RDAPHttpQueryTypeProcessor.RDAPHttpQueryType> queryTypeProcessor = Mockito.mockStatic(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.class);
    queryTypeProcessor.when(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType::values)
            .thenReturn(new RDAPHttpQueryTypeProcessor.RDAPHttpQueryType[] {
                    RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.DOMAIN,
                    RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.IP,
                    RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.AUTNUM,
                    RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.ENTITY,
                    RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVER,
                    RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVERS,
                    RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.HELP});

    when(mockConfig.getUri()).thenReturn(uri);
    when(mockConfig.getTimeout()).thenReturn(1000);
    when(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.getType(uri.toString())).thenReturn(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.IP);

    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(404);
    when(mockResponse.uri()).thenReturn(URI.create("http://example.com/domain/test.invalid"));

    mockStaticRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(mockResponse);

    boolean result = responseValidator.doValidate();
    assertTrue(result);

    queryTypeProcessor.close();
  }

  @Test
  public void testDoValidate_WithErrors_InResultFile() throws Exception {
    // Use real RDAPValidatorResults instead of mock so we can check the actual results
    QueryContext testContext = QueryContext.forTesting(mockConfig);
    RDAPValidatorResults realResults = testContext.getResults();
    responseValidator = new ResponseValidationDomainInvalid_2024(testContext);

    URI uri = new URI("http://example.com/rdap");
    when(mockConfig.getUri()).thenReturn(uri);

    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn("{\"rdapConformance\":[]}");
    when(response.uri()).thenReturn(URI.create("http://example.com/domain/test.invalid"));

    mockStaticRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(response);

    assertThat(responseValidator.doValidate()).isFalse();

    // Now check the real results
    assertThat(realResults.getAll().stream().anyMatch(result ->
            result.getCode() == -65300 &&
                    result.getMessage().equals("A query for an invalid domain name did not yield a 404 response.")
    )).isTrue();
  }

  @Test
  public void testValidateInvalidDomainQuery_ValidJson_OkStatus() {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    String jsonBody = "{\"rdapConformance\":[], \"errorCode\":404}";
    when(mockResponse.body()).thenReturn(jsonBody);

    boolean result = responseValidator.validateDomainInvalidQuery(mockResponse, true);
    assertTrue(result);
  }

  @Test
  public void testValidateInvalidDomainQuery_InvalidJson() {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.uri()).thenReturn(URI.create("http://example.com/domain/test.invalid"));
    String jsonBody = "{}";
    when(mockResponse.body()).thenReturn(jsonBody);

    boolean result = responseValidator.validateDomainInvalidQuery(mockResponse, true);
    assertFalse(result);
  }
}