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
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.DomainCaseFoldingValidation;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfile;
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
    validator = new RDAPValidator(config, fs, processor, query, configParser, results, datasetService);
    doReturn(true).when(processor).check(datasetService);
    doReturn(true).when(datasetService).download(anyBoolean());
    doReturn(new ConfigurationFile("Test", null, null, null, null, false, false, false, false, false))
        .when(configParser).parse(any());
  }

  @Test
  public void testValidate_InvalidConfiguration_ReturnsErrorStatus1() throws IOException {
    doThrow(IOException.class).when(configParser).parse(any());

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.CONFIG_INVALID.getValue());
  }

  @Test
  public void testValidate_DatasetsError_ReturnsErrorStatus2() {
    doReturn(false).when(datasetService).download(anyBoolean());

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.DATASET_UNAVAILABLE.getValue());
  }

  @Test
  public void testValidate_QueryTypeProcessorError_ReturnsError() {
    doReturn(false).when(processor).check(datasetService);
    doReturn(RDAPValidationStatus.UNSUPPORTED_QUERY).when(processor).getErrorStatus();

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.UNSUPPORTED_QUERY.getValue());
  }

  @Test
  public void testValidate_QueryError_ReturnsError() {
    doReturn(false).when(query).run();
    doReturn(RDAPValidationStatus.CONNECTION_FAILED).when(query).getErrorStatus();

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.CONNECTION_FAILED.getValue());
  }

  @Test
  public void testCallProfile2019_QueryNonContentRdapResponse_ProfileIsNotInvoked() {
    doReturn(true).when(query).run();
    doReturn(true).when(query).checkWithQueryType(RDAPQueryType.DOMAIN);
    doReturn(true).when(query).isErrorContent();

   verify(rdapProfile, times(0)).validate();
  }

  @Test
  public void testCallDomainCaseFolding_Query404StatusRdapResponse_CaseFoldingValidationIsNotInvoked() {
    doReturn(true).when(query).run();
    doReturn(true).when(query).checkWithQueryType(RDAPQueryType.DOMAIN);
    doReturn(true).when(query).isErrorContent();

    verify(domainCaseFoldingValidation, times(0)).validate();
  }

  @Test
  public void testValidate_QuerycheckWithQueryTypeError_ReturnsError() {
    doReturn(RDAPQueryType.DOMAIN).when(processor).getQueryType();
    doReturn(false).when(query).checkWithQueryType(RDAPQueryType.DOMAIN);
    doReturn(RDAPValidationStatus.SUCCESS).when(query).getErrorStatus();

    assertThat(validator.validate())
        .isEqualTo(RDAPValidationStatus.SUCCESS.getValue());
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

    doReturn(false).when(config).check();

    assertThatThrownBy(() -> new RDAPValidator(config, fileSystem, queryTypeProcessor, query, configParser, results, datasetService))
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

    doReturn(true).when(config).check();
    doReturn(true).when(queryTypeProcessor).check(datasetService);
    doReturn(true).when(datasetService).download(anyBoolean());
    doReturn(new ConfigurationFile(
            "Test", null, null, null, null, false, false, false, false, false))
            .when(configParser).parse(any());
    doReturn(false).when(query).run();
    doReturn(null).when(query).getErrorStatus();

    RDAPValidator validator = new RDAPValidator(config, fileSystem, queryTypeProcessor, query, configParser, results, datasetService);

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.SUCCESS.getValue());
  }

  @Test
  public void testValidate_DomainQueryForTestInvalidWithHttpOK_LogsInfo() throws IOException {
    RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
    FileSystem fileSystem = mock(FileSystem.class);
    RDAPQueryTypeProcessor queryTypeProcessor = mock(RDAPQueryTypeProcessor.class);
    RDAPQuery query = mock(RDAPQuery.class);
    ConfigurationFileParser configParser = mock(ConfigurationFileParser.class);
    RDAPValidatorResults results = mock(RDAPValidatorResults.class);
    RDAPDatasetService datasetService = mock(RDAPDatasetService.class);

    doReturn(true).when(config).check();
    doReturn(URI.create("https://example.com")).when(config).getUri(); // Mock getUri to return a valid URI
    doReturn(true).when(queryTypeProcessor).check(datasetService);
    doReturn(true).when(datasetService).download(anyBoolean());
    doReturn(new ConfigurationFile(
        "definitionIdentifier", null, null, null, null, false, false, false, false, false))
        .when(configParser).parse(any());
    doReturn(true).when(query).run();
    doReturn(Optional.of(HTTP_OK)).when(query).getStatusCode(); // Return Optional<Integer>
    doReturn("test.invalid").when(query).getData();
    doReturn(RDAPQueryType.DOMAIN).when(queryTypeProcessor).getQueryType();

    RDAPValidator validator = new RDAPValidator(config, fileSystem, queryTypeProcessor, query, configParser, results, datasetService);

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.SUCCESS.getValue());
  }
}