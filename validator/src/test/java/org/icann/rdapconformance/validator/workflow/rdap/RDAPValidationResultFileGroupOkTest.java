package org.icann.rdapconformance.validator.workflow.rdap;

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

  private FileSystem fileSystem;
  private RDAPValidationResultFile file;

  @BeforeMethod
  public void setUp() {
    RDAPValidatorResults results = new RDAPValidatorResults();
    fileSystem = mock(FileSystem.class);
    results.addGroups(Set.of("firstGroup"));
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
}