package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.http.HttpResponse;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class ResponseValidationHelp_2024Test {

  private RDAPValidatorConfiguration mockConfig;
  private RDAPValidatorResults mockResults;
  private ResponseValidationHelp_2024 responseValidator;

  @BeforeMethod
  public void setup() {
    mockConfig = mock(RDAPValidatorConfiguration.class);
    mockResults = mock(RDAPValidatorResults.class);
    responseValidator = new ResponseValidationHelp_2024(mockConfig, mockResults);
  }

  @Test
  public void testGetGroupName() {
    assertEquals(responseValidator.getGroupName(), "rdapResponseHelp_2024_Validation");
  }

  @Test
  public void testDoValidate_HelpTypeUrl_WithHelpInUri() throws Exception {
    URI uri = new URI("https://apis.cscglobal.com/dbs/rdap-api/v1/domain/CSCGLOBAL.COM");
    when(mockConfig.getUri()).thenReturn(uri);
    when(mockConfig.getTimeout()).thenReturn(1000);

    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(200);
    when(mockResponse.body()).thenReturn("{\"rdapConformance\":[],\"notices\":[]}");

    boolean result = responseValidator.doValidate();
    assertTrue(result);
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
    String jsonBody = "{}"; // Missing required keys
    when(mockResponse.body()).thenReturn(jsonBody);

    boolean result = responseValidator.validateHelpQuery(mockResponse, true);
    assertFalse(result);
  }

  @Test
  public void testValidateHelpQuery_NotOkStatus() {
    HttpResponse<String> mockResponse = mock(HttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(404);
    when(mockResponse.body()).thenReturn("{\"rdapConformance\":[],\"notices\":[]}");

    boolean result = responseValidator.validateHelpQuery(mockResponse, true);
    assertFalse(result);
  }
}