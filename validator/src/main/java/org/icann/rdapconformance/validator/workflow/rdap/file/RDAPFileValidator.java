package org.icann.rdapconformance.validator.workflow.rdap.file;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;

public class RDAPFileValidator extends RDAPValidator {

  public RDAPFileValidator(RDAPValidatorConfiguration config, FileSystem fileSystem) {
    super(config, fileSystem,
        new RDAPFileQuery(config, fileSystem));
  }
}
