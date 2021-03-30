package org.icann.rdapconformance.validator;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service locator for RDAP validation.
 */
public class RDAPValidatorContext {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidatorContext.class);
  private final ConfigurationFile configurationFile;
  private final RDAPDeserializer deserializer;
  private final Set<RDAPValidationResult> results = new HashSet<>();

  public RDAPValidatorContext(ConfigurationFile configurationFile) {
    this.configurationFile = configurationFile;
    this.deserializer = new RDAPDeserializer(this);
  }

  public boolean isTestEnabled(int testCode) {
    return !configurationFile.getDefinitionIgnore().contains(testCode);
  }

  public void addResult(RDAPValidationResult result) {
    logger.error("adding error result {}", result);
    this.results.add(result);
  }

  public Set<RDAPValidationResult> getResults() {
    return results;
  }
}
