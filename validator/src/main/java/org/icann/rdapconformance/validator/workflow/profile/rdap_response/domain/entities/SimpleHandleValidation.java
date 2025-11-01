package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.rdap_response.HandleValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

/**
 * When HandleValidation will be a concrete class, please suppress this one.
 */
public class SimpleHandleValidation extends HandleValidation {

  // QueryContext constructor for production use
  public SimpleHandleValidation(QueryContext queryContext, int code) {
    super(queryContext, code, "entity");
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
