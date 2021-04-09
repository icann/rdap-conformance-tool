package org.icann.rdapconformance.validator.workflow;

public interface ValidatorWorkflow {

  /**
   * Validate the JSON data.
   *
   * @return The error status
   */
  int validate();

}
