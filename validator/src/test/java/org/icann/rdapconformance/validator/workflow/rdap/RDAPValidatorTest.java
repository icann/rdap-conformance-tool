package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class RDAPValidatorTest {

  private final RDAPValidatorConfiguration config = mock(RDAPValidatorConfiguration.class);
  private final FileSystem fs = mock(FileSystem.class);
  private final RDAPQueryTypeProcessor processor = mock(RDAPQueryTypeProcessor.class);
  private final RDAPQuery query = mock(RDAPQuery.class);
  private RDAPValidator validator;

  @BeforeMethod
  public void setUp() throws IOException {
    File configFile = mock(File.class);
    doReturn(true).when(config).check();
    validator = new RDAPValidator(config, fs, processor, query) {

    };
    doReturn("config/path.json").when(configFile).getAbsolutePath();
    doReturn(configFile).when(config).getConfigurationFile();
    doReturn("{\"definitionIdentifier\": \"rdapct test\"}").when(fs).readFile("config/path.json");
    doReturn(true).when(processor).check();
  }

  @Test
  public void testValidate_InvalidConfiguration_ReturnsErrorStatus1() throws IOException {
    doReturn("{}").when(fs).readFile("config/path.json");

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.CONFIG_INVALID.getValue());
  }

  @Test
  @Ignore("Not implemented yet")
  public void testValidate_DatasetsError_ReturnsErrorStatus2() {

    assertThat(validator.validate()).isEqualTo(RDAPValidationStatus.DATASET_UNAVAILABLE.getValue());
  }

  @Test
  public void testValidate_QueryTypeProcessorError_ReturnsError() {
    doReturn(false).when(processor).check();
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
  public void testValidate_QuerycheckWithQueryTypeError_ReturnsError() {
    doReturn(RDAPQueryType.DOMAIN).when(processor).getQueryType();
    doReturn(false).when(query).checkWithQueryType(RDAPQueryType.DOMAIN);
    doReturn(RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND).when(query).getErrorStatus();

    assertThat(validator.validate())
        .isEqualTo(RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND.getValue());
  }
}