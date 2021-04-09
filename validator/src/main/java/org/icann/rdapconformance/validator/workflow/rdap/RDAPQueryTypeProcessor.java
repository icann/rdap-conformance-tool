package org.icann.rdapconformance.validator.workflow.rdap;

public interface RDAPQueryTypeProcessor {

  /**
   * Check the RDAP query type is valid.
   */
  boolean check();

  /**
   * Get the error status when query type is invalid.
   */
  RDAPValidationStatus getErrorStatus();

  /**
   * Get the query type.
   */
  RDAPQueryType getQueryType();
}
