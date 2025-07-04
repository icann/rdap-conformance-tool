package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

/**
 * When HandleValidation will be a concrete class, please suppress this one.
 */
public class SimpleHandleValidation extends HandleValidation {

  public SimpleHandleValidation(RDAPValidatorConfiguration config, String rdapResponse,
                                RDAPValidatorResults results,
                                RDAPDatasetService datasetService,
                                RDAPQueryType queryType, int code) {
    super(config, rdapResponse, results, datasetService, queryType, code, "entity");
  }

  @Override
  public String getGroupName() {
    throw new IllegalArgumentException("Should not be called. This class exists only because Java"
        + " does not support multiple inheritance");
  }

  @Override
  protected boolean doValidate() {
    throw new IllegalArgumentException("Should not be called. This class exists only because Java"
        + " does not support multiple inheritance");
  }

  public boolean validateHandle(String handleJsonPointer) {
    return super.validateHandle(handleJsonPointer);
  }
}
