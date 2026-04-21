package org.icann.rdapconformance.validator.workflow.rdap;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.icann.rdapconformance.validator.ConnectionStatus;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.DomainCaseFoldingValidation;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfile;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPValidatorTest {

  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
  private final FileSystem fs = mock(FileSystem.class);
  private final RDAPQueryTypeProcessor processor = mock(RDAPQueryTypeProcessor.class);
  private final RDAPQuery query = mock(RDAPQuery.class);
  private final ConfigurationFileParser configParser = mock(ConfigurationFileParser.class);
  private final RDAPValidatorResults results = mock(RDAPValidatorResults.class);
  private final RDAPDatasetService datasetService = mock(RDAPDatasetService.class);
  private final RDAPProfile rdapProfile = mock(RDAPProfile.class);
  private final DomainCaseFoldingValidation domainCaseFoldingValidation = mock(DomainCaseFoldingValidation.class);
  private RDAPValidator validator;

  @BeforeMethod
  public void setUp() throws IOException {
    doReturn(true).when(config).check();
    doReturn(URI.create("https://example.com")).when(config).getUri(); // Mock getUri to return a valid URI
    validator = new RDAPValidator(QueryContext.create(config, datasetService, query));
    doReturn(true).when(processor).check(eq(datasetService), any(QueryContext.class));
    doReturn(true).when(datasetService).download(anyBoolean());
    doReturn(new ConfigurationFile("Test", null, null, null, null, false, false, false, false, false))
        .when(configParser).parse(any());
  }

  @Test
  public void testValidate_QueryError_ReturnsError() {
    doReturn(false).when(query).run();
    doReturn(ConnectionStatus.CONNECTION_FAILED).when(query).getErrorStatus();

    assertThat(validator.validate()).isEqualTo(ConnectionStatus.CONNECTION_FAILED.getCode());
  }

  @Test
  public void testCallProfile2019_QueryNonContentRdapResponse_ProfileIsNotInvoked() {
    doReturn(true).when(query).run();
    doReturn(true).when(query).validateStructureByQueryType(RDAPQueryType.DOMAIN);
    doReturn(true).when(query).isErrorContent();

   verify(rdapProfile, times(0)).validate();
  }

  @Test
  public void testCallDomainCaseFolding_Query404StatusRdapResponse_CaseFoldingValidationIsNotInvoked() {
    doReturn(true).when(query).run();
    doReturn(true).when(query).validateStructureByQueryType(RDAPQueryType.DOMAIN);
    doReturn(true).when(query).isErrorContent();

    verify(domainCaseFoldingValidation, times(0)).validate();
  }

  @Test
  public void testValidate_QuerycheckWithQueryTypeError_ReturnsError() {
    doReturn(RDAPQueryType.DOMAIN).when(processor).getQueryType();
    doReturn(false).when(query).validateStructureByQueryType(RDAPQueryType.DOMAIN);
    doReturn(ToolResult.SUCCESS).when(query).getErrorStatus();

    assertThat(validator.validate())
        .isEqualTo(ToolResult.SUCCESS.getCode());
  }


  @Test
  public void testConstructor_ConfigCheckFails_ThrowsRuntimeException() {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    FileSystem fileSystem = mock(FileSystem.class);
    RDAPQueryTypeProcessor queryTypeProcessor = mock(RDAPQueryTypeProcessor.class);
    RDAPQuery query = mock(RDAPQuery.class);
    ConfigurationFileParser configParser = mock(ConfigurationFileParser.class);
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    RDAPDatasetService datasetService = mock(RDAPDatasetService.class);

    // Provide URI for QueryContext creation but make config.check() fail
    doReturn(java.net.URI.create("https://example.com/domain/test.example")).when(config).getUri();
    doReturn(false).when(config).check();

    assertThatThrownBy(() -> new RDAPValidator(QueryContext.create(config, datasetService, query)))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Please fix the configuration");
  }

  @Test
  public void testValidate_QueryRunReturnsFalseAndErrorStatusIsNull_ReturnsSuccess() throws IOException {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    FileSystem fileSystem = mock(FileSystem.class);
    RDAPQueryTypeProcessor queryTypeProcessor = mock(RDAPQueryTypeProcessor.class);
    RDAPQuery query = mock(RDAPQuery.class);
    ConfigurationFileParser configParser = mock(ConfigurationFileParser.class);
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    RDAPDatasetService datasetService = mock(RDAPDatasetService.class);

    // Provide URI for QueryContext creation
    doReturn(java.net.URI.create("https://example.com/domain/test.example")).when(config).getUri();
    doReturn(true).when(config).check();
    doReturn(true).when(queryTypeProcessor).check(eq(datasetService), any(QueryContext.class));
    doReturn(true).when(datasetService).download(anyBoolean());
    doReturn(new ConfigurationFile(
            "Test", null, null, null, null, false, false, false, false, false))
            .when(configParser).parse(any());
    doReturn(false).when(query).run();
    doReturn(null).when(query).getErrorStatus();

    RDAPValidator validator = new RDAPValidator(QueryContext.create(config, datasetService, query));

    assertThat(validator.validate()).isEqualTo(ToolResult.SUCCESS.getCode());
  }

  // fails
@Test
public void testValidate_DomainQueryForTestInvalidWithHttpOK_LogsInfo() throws IOException {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    FileSystem fileSystem = mock(FileSystem.class);
    RDAPQuery query = mock(RDAPQuery.class);
    ConfigurationFileParser configParser = mock(ConfigurationFileParser.class);
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    RDAPDatasetService datasetService = mock(RDAPDatasetService.class);

    RDAPHttpRequest.SimpleHttpResponse mockResponse = mock(RDAPHttpRequest.SimpleHttpResponse.class);
    when(mockResponse.statusCode()).thenReturn(HTTP_OK);
    when(mockResponse.body()).thenReturn("{}");
    when(mockResponse.uri()).thenReturn(URI.create("https://example.com/rdap/domain/test.invalid"));

    doReturn(true).when(config).check();
    doReturn(URI.create("https://example.com")).when(config).getUri();
    doReturn(true).when(datasetService).download(anyBoolean());
    doReturn(new ConfigurationFile(
        "definitionIdentifier", null, null, null, null, false, false, false, false, false))
        .when(configParser).parse(any());
    doReturn(true).when(query).run();
    doReturn("test.invalid").when(query).getData();
    doReturn(mockResponse).when(query).getRawResponse();
    doReturn(false).when(query).isErrorContent();

    // Create validator with real dependencies
    RDAPValidator validator = new RDAPValidator(QueryContext.create(config, datasetService, query));

    // Note: queryTypeProcessor is now encapsulated within QueryContext
    // The validator will use its QueryContext's processor instead of a singleton

    assertThat(validator.validate()).isEqualTo(ToolResult.SUCCESS.getCode());
    }

    @Test
    public void testValidate_TwoRoundsOnSameContext_SecondRoundDoesNotInheritFirstRoundHttpStatus() {
        // Simulate the regression: first round succeeds with 200, second round fails
        // (e.g., server rate-limits with 429). Without the fix, the second round's
        // RDAPValidationResult.build(queryContext) would inherit the 200 status from round 1.

        RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
        RDAPQuery query = mock(RDAPQuery.class);
        RDAPDatasetService datasetService = mock(RDAPDatasetService.class);

        doReturn(URI.create("https://example.com/rdap/domain/test.example")).when(config).getUri();
        doReturn(true).when(config).check();
        doReturn(false).when(config).useRdapProfileFeb2019();
        doReturn(false).when(config).useRdapProfileFeb2024();
        doReturn(false).when(config).isAdditionalConformanceQueries();
        doReturn(false).when(config).isNetworkEnabled();
        doReturn(true).when(datasetService).download(anyBoolean());

        // Round 1: query succeeds, raw response has HTTP 200
        RDAPHttpRequest.SimpleHttpResponse round1Response = mock(RDAPHttpRequest.SimpleHttpResponse.class);
        when(round1Response.statusCode()).thenReturn(HTTP_OK);
        when(round1Response.body()).thenReturn("{\"objectClassName\":\"domain\"}");
        when(round1Response.uri()).thenReturn(URI.create("https://example.com/rdap/domain/test.example"));

        // Round 2: query fails (run() returns false), simulating a 429 or network error
        // At this point getRawResponse() returns null — no new response was set
        doReturn(false).when(query).run();
        doReturn(null).when(query).getErrorStatus();   // null → ToolResult.SUCCESS path
        doReturn(null).when(query).getRawResponse();

        QueryContext queryContext = QueryContext.create(config, datasetService, query);
        RDAPValidator validator = new RDAPValidator(queryContext);

        // Round 1: manually prime currentHttpResponse as if round 1 had succeeded
        queryContext.setCurrentHttpResponse(round1Response);

        // Round 2: call validate() again on the same QueryContext
        validator.validate();

        // After the fix, currentHttpResponse must be null at the start of round 2,
        // so no stale HTTP 200 leaks into error results produced in this round.
        assertThat(queryContext.getCurrentHttpResponse())
                .as("currentHttpResponse should be null after a failed round; stale response from round 1 must not persist")
                .isNull();
    }
}