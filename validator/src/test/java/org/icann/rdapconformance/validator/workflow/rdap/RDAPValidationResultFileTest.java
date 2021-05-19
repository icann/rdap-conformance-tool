package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPValidationResultFileTest {

  private FileSystem fileSystem;
  private RDAPValidationResultFile file;
  private RDAPValidatorResults results;
  private ConfigurationFile configurationFile;

  @BeforeMethod
  public void setUp() {
    results = new RDAPValidatorResults();
    fileSystem = mock(FileSystem.class);
    results.addGroups(Set.of("firstGroup"));
    configurationFile = mock(ConfigurationFile.class);
    file = new RDAPValidationResultFile(
        results,
        mock(RDAPValidatorConfiguration.class),
        configurationFile,
        fileSystem);
  }

  @Test
  public void testGroupOkAssigned() throws IOException {
    file.build(200);
    verify(fileSystem).write(any(), contains("\"groupOK\": [\"firstGroup\"]"));
  }

  @Test
  public void testGroupErrorWarningAssigned() throws IOException {
    results.addGroupErrorWarning("secondGroup");
    file.build(200);
    verify(fileSystem).write(any(), contains("\"groupErrorWarning\": [\"secondGroup\"]"));
  }

  @Test
  public void testIgnore() throws IOException {
    int ignoredCode = -1000;
    results.add(RDAPValidationResult.builder()
        .code(ignoredCode)
        .value("ignoreCode")
        .message("this is a code to ignore")
        .build());
    doReturn(List.of(ignoredCode)).when(configurationFile).getDefinitionIgnore();
    file.build(200);
    // error should be an empty list since the only result code must be ignored:
    verify(fileSystem).write(any(), contains("\"error\": []"));
  }
}