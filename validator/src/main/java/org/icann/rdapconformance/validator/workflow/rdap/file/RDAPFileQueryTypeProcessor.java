package org.icann.rdapconformance.validator.workflow.rdap.file;

import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;

public class RDAPFileQueryTypeProcessor implements RDAPQueryTypeProcessor {

  private RDAPValidatorConfiguration config;

  // Public constructor for instance-based usage
  public RDAPFileQueryTypeProcessor() {
  }

  // Constructor with configuration
  public RDAPFileQueryTypeProcessor(RDAPValidatorConfiguration config) {
    this.config = config;
  }

  // Method to set the configuration
  public void setConfiguration(RDAPValidatorConfiguration config) {
    this.config = config;
  }

  @Override
  public boolean check(
      RDAPDatasetService datasetService) {
    return true;
  }

  @Override
  public ToolResult getErrorStatus() {
    return null;
  }

  @Override
  public RDAPQueryType getQueryType() {
    return config.getQueryType();
  }
}