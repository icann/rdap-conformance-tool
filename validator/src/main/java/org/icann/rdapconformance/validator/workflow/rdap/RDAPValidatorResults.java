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

  private final Set<String> groups = new HashSet<>();
  private final Set<String> groupErrorWarning = new HashSet<>();

  public void add(RDAPValidationResult result) {
    if (this.results.add(result)) {
      logger.error("adding error result {}", result);
    }
  }

  public Set<RDAPValidationResult> getAll() {
    return results;
  }

  public boolean isEmpty() {
    return results.isEmpty();
  }

  public Set<String> getGroupOk() {
    Set<String> groupsCopy = new HashSet<>(groups);
    groupsCopy.removeAll(groupErrorWarning);
    return groupsCopy;
  }

  public void addGroups(Set<String> groups) {
    this.groups.addAll(groups);
  }

  public Set<String> getGroupErrorWarning() {
    return groupErrorWarning;
  }

  public void addGroupErrorWarning(String validationName) {
    groupErrorWarning.add(validationName);
  }

  public Set<String> getGroups() {
    return groups;
  }

  public void addGroup(String group, boolean hasError) {
    this.groups.add(group);
    if (hasError) {
      this.groupErrorWarning.add(group);
    }
  }
}
