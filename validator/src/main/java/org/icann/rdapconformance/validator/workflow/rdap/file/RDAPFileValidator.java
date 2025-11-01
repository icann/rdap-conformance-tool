package org.icann.rdapconformance.validator.workflow.rdap.file;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;

public class RDAPFileValidator extends RDAPValidator {

  public RDAPFileValidator(RDAPValidatorConfiguration config, RDAPDatasetService datasetService) {
    super(QueryContext.create(config, datasetService, new RDAPFileQuery(config, datasetService)));
  }
}
