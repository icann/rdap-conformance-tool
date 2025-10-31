package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.QueryContext;
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

public class ResponseValidationHelp_2024Test {

  private RDAPValidatorConfiguration mockConfig;
  private RDAPValidatorResults results;
  private ResponseValidationHelp_2024 responseValidator;
  private MockedStatic<RDAPHttpRequest> mockStaticRequest;
  private QueryContext queryContext;

  @BeforeMethod
  public void setup() {
    mockConfig = mock(RDAPValidatorConfiguration.class);
    queryContext = QueryContext.forTesting(mockConfig);
    results = queryContext.getResults();
    results.clear();
    responseValidator = new ResponseValidationHelp_2024(queryContext);
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
    assertEquals(responseValidator.getGroupName(), "rdapResponseHelp_2024_Validation");
  }

  @Test
  public void testDoValidate_HelpTypeUrl_WithHelpInUri() throws Exception {
    URI uri = new URI("http://example.com/rdap");
    when(mockConfig.getUri()).thenReturn(uri);
    when(mockConfig.getTimeout()).thenReturn(1000);

    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{\"rdapConformance\":[],\"notices\":[]}");
    when(mockResponse.uri()).thenReturn(URI.create("http://example.com/help"));

    mockStaticRequest = mockStatic(RDAPHttpRequest.class);
    mockStaticRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(mockResponse);

    boolean result = responseValidator.doValidate();
    assertTrue(result);
  }

  @Test
  public void testDoValidate_HelpTypeUrl_WithHelpInUri_AndQueryType() throws Exception {
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
    when(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.getType(uri.toString())).thenReturn(RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.DOMAIN);

    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{\"rdapConformance\":[],\"notices\":[]}");
    when(mockResponse.uri()).thenReturn(URI.create("http://example.com/help"));

    mockStaticRequest = mockStatic(RDAPHttpRequest.class);
    mockStaticRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(mockResponse);

    boolean result = responseValidator.doValidate();
    assertTrue(result);

    queryTypeProcessor.close();
  }

  @Test
  public void testDoValidate_WithErrors_InResultFile() throws Exception {
    URI uri = new URI("http://example.com/rdap");
    when(mockConfig.getUri()).thenReturn(uri);

    HttpResponse<String> response = mock(HttpResponse.class);
    when(response.statusCode()).thenReturn(200);
    when(response.body()).thenReturn("{\"rdapConformance\":[]}");
    when(response.uri()).thenReturn(URI.create("http://example.com/help"));

    mockStaticRequest = mockStatic(RDAPHttpRequest.class);
    mockStaticRequest.when(() -> RDAPHttpRequest.makeHttpGetRequest(any(), anyInt())).thenReturn(response);

    assertThat(responseValidator.doValidate()).isFalse();

    ArgumentCaptor<RDAPValidationResult> resultCaptor = ArgumentCaptor.forClass(RDAPValidationResult.class);
    assertThat(results.getAll().stream().anyMatch(result ->
            result.getCode() == -20701 &&
                    result.getMessage().equals("Response to a /help query did not yield a proper status code or RDAP response.")
    )).isTrue();
  }

  @Test
  public void testValidateHelpQuery_ValidJson_OkStatus() {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    String jsonBody = "{\"rdapConformance\":[],\"notices\":[]}";
    when(mockResponse.body()).thenReturn(jsonBody);

    boolean result = responseValidator.validateHelpQuery(mockResponse, true);
    assertTrue(result);
  }

  @Test
  public void testValidateHelpQuery_InvalidJson() {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.uri()).thenReturn(URI.create("http://example.com/help"));
    String jsonBody = "{}"; // Missing required keys
    when(mockResponse.body()).thenReturn(jsonBody);

    boolean result = responseValidator.validateHelpQuery(mockResponse, true);
    assertFalse(result);
  }

  @Test
  public void testValidateHelpQuery_NotOkStatus() {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(404);
    when(mockResponse.uri()).thenReturn(URI.create("http://example.com/help"));
    when(mockResponse.body()).thenReturn("{\"rdapConformance\":[],\"notices\":[]}");

    boolean result = responseValidator.validateHelpQuery(mockResponse, true);
    assertFalse(result);
  }
}