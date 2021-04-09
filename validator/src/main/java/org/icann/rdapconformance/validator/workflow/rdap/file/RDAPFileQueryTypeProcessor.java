package org.icann.rdapconformance.validator.workflow.rdap.file;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;

public class RDAPFileQueryTypeProcessor implements RDAPQueryTypeProcessor {

  private final RDAPValidatorConfiguration config;

  public RDAPFileQueryTypeProcessor(RDAPValidatorConfiguration config) {
    this.config = config;
  }

  @Override
  public boolean check() {
    return true;
  }

  @Override
  public RDAPValidationStatus getErrorStatus() {
    return null;
  }

  @Override
  public RDAPQueryType getQueryType() {
    return config.getQueryType();
  }
}
