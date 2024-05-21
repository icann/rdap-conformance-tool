package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPValidatorResults {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidatorResults.class);

  private final Set<RDAPValidationResult> results = new HashSet<>();

  private final Set<String> groups = new HashSet<>();
  private final Set<String> groupErrorWarning = new HashSet<>();

  public void add(RDAPValidationResult result) {
    if (this.results.add(result)) {
      logger.debug("adding error result {}", result);
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

  public void addGroup(String group) {
    this.groups.add(group);
  }

  public void addGroupErrorWarning(String group) {
    this.addGroup(group);
    this.groupErrorWarning.add(group);
  }

  public Set<String> getGroups() {
    return groups;
  }
}
