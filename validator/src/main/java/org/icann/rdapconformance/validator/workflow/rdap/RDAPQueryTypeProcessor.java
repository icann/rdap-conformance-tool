package org.icann.rdapconformance.validator.workflow.rdap;

public interface RDAPQueryTypeProcessor {

  /**
   * Check the RDAP query type is valid.
   * @param datasetService
   */
  boolean check(RDAPDatasetService datasetService);

  /**
   * Get the error status when query type is invalid.
   */
  RDAPValidationStatus getErrorStatus();

  /**
   * Get the query type.
   */
  RDAPQueryType getQueryType();
}
