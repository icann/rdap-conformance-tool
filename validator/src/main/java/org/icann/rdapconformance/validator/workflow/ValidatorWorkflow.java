package org.icann.rdapconformance.validator.workflow;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public interface ValidatorWorkflow {

  /**
   * Validate the JSON data.
   *
   * @return The error status
   */
  int validate();
  RDAPValidatorResults getResults();

  /**
   * Get the results path.
   * @return the results path
   */
  String getResultsPath();

}
