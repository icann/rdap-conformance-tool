package org.icann.rdapconformance.validator.workflow;

public interface ValidatorWorkflow {

  /**
   * Validate the JSON data.
   *
   * @return The error status
   */
  int validate();

  /**
   * Get the results path.
   * @return the results path
   */
  String getResultsPath();

}
