package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.profile.RDAPProfileFebruary2019;
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
  private final RDAPProfileFebruary2019 rdapProfileFebruary2019 = mock(RDAPProfileFebruary2019.class);
  private RDAPValidator validator;

  @BeforeMethod
  public void setUp() throws IOException {
    doReturn(true).when(config).check();
    validator = new RDAPValidator(config, fs, processor, query, configParser, results,
        datasetService) {

    };
    doReturn(true).when(processor).check(datasetService);
    doReturn(true).when(datasetService).download(anyBoolean());
    doReturn(new ConfigurationFile("Test", null, null, null, null)).when(configParser).parse(any());
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

   verify(rdapProfileFebruary2019, times(0)).validate();
  }

  @Test
  public void testValidate_QuerycheckWithQueryTypeError_ReturnsError() {
    doReturn(RDAPQueryType.DOMAIN).when(processor).getQueryType();
    doReturn(false).when(query).checkWithQueryType(RDAPQueryType.DOMAIN);
    doReturn(RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND).when(query).getErrorStatus();

    assertThat(validator.validate())
        .isEqualTo(RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND.getValue());
  }
}