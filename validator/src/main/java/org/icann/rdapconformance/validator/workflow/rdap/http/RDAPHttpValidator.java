package org.icann.rdapconformance.validator.workflow.rdap.http;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;

public class RDAPHttpValidator extends RDAPValidator {

  public RDAPHttpValidator(RDAPValidatorConfiguration config, FileSystem fileSystem) {
    super(config, fileSystem, new RDAPHttpQueryTypeProcessor(config), new RDAPHttpQuery(config));
  }
}
