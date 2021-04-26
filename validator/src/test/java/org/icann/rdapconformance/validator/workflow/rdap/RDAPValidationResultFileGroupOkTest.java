package org.icann.rdapconformance.validator.workflow.rdap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPValidationResultFileGroupOkTest {

  private static String THE_GROUP_OK = "firstGroup";
  private RDAPValidatorResults results;
  private FileSystem fileSystem;
  private RDAPValidationResultFile file;

  @BeforeMethod
  public void setUp() {
    results = new RDAPValidatorResults();
    fileSystem = mock(FileSystem.class);
    results.setGroups(Set.of(THE_GROUP_OK));
    file = new RDAPValidationResultFile(
        results,
        mock(RDAPValidatorConfiguration.class),
        mock(ConfigurationFile.class),
        fileSystem);
  }

  @Test
  public void testGroupOkAssigned() throws IOException {
    file.build(200);
    verify(fileSystem).write(any(), contains("\"groupOK\": [\"firstGroup\"]"));
  }

  @Test
  public void testCreateGroupOk() {
    assertThat(file.createGroupOk()).containsExactly(THE_GROUP_OK);
  }
}