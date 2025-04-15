package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPValidatorResultsImpl implements RDAPValidatorResults {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidatorResults.class);

  // Static instance for the singleton
  private static RDAPValidatorResultsImpl instance;

  private final Set<RDAPValidationResult> results = new HashSet<>();
  private final Set<String> groups = new HashSet<>();
  private final Set<String> groupErrorWarning = new HashSet<>();

  // Private constructor to prevent instantiation
  private RDAPValidatorResultsImpl() {}

  /**
   * Gets the singleton instance of RDAPValidatorResultsImpl
   *
   * @return the singleton instance
   */
  public static synchronized RDAPValidatorResultsImpl getInstance() {
    if (instance == null) {
      instance = new RDAPValidatorResultsImpl();
    }
    return instance;
  }

  /**
   * Resets the singleton instance (primarily for testing)
   */
  public static void reset() {
    instance = null;
  }

  @Override
  public void add(RDAPValidationResult result) {
    if (this.results.add(result)) {
      logger.debug("adding error result {}", result);
    }
  }

  @Override
  public Set<RDAPValidationResult> getAll() {
    return results;
  }

  @Override
  public boolean isEmpty() {
    return results.isEmpty();
  }

  @Override
  public Set<String> getGroupOk() {
    Set<String> groupsCopy = new HashSet<>(groups);
    groupsCopy.removeAll(groupErrorWarning);
    return groupsCopy;
  }

  @Override
  public void addGroups(Set<String> groups) {
    this.groups.addAll(groups);
  }

  @Override
  public Set<String> getGroupErrorWarning() {
    return groupErrorWarning;
  }

  @Override
  public void addGroup(String group) {
    this.groups.add(group);
  }

  @Override
  public void addGroupErrorWarning(String group) {
    this.addGroup(group);
    this.groupErrorWarning.add(group);
  }

  @Override
  public Set<String> getGroups() {
    return groups;
  }

  /**
   * Clears all results and groups from the instance
   *  should only be used in testing as well
   */
  public void clear() {
    results.clear();
    groups.clear();
    groupErrorWarning.clear();
  }
}