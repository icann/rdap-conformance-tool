package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service locator for RDAP validation.
 */
public class RDAPValidatorResults {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidatorResults.class);

  private final Set<RDAPValidationResult> results = new HashSet<>();

  public RDAPValidatorResults() {
  }

  public void add(RDAPValidationResult result) {
    logger.error("adding error result {}", result);
    this.results.add(result);
  }

  public Set<RDAPValidationResult> getAll() {
    return results;
  }
}
