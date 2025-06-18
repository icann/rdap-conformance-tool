package org.icann.rdapconformance.validator.workflow.rdap.http;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;

public class RDAPHttpValidator extends RDAPValidator {

  public RDAPHttpValidator(RDAPValidatorConfiguration config, RDAPDatasetService datasetService) {
    super(config, new RDAPHttpQuery(config),datasetService );
  }
}
