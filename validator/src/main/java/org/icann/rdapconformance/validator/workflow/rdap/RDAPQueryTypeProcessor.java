package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.ToolResult;

public interface RDAPQueryTypeProcessor {

  /**
   * Check the RDAP query type is valid.
   */
  boolean check(RDAPDatasetService datasetService);

  /**
   * Get the error status when query type is invalid.
   */
  ToolResult getErrorStatus();

  /**
   * Get the query type.
   */
  RDAPQueryType getQueryType();
}
