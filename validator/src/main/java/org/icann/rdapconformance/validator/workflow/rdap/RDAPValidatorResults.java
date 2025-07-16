package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.Set;

public interface RDAPValidatorResults {


  int getResultCount();

  void add(RDAPValidationResult result);

  void addAll(Set<RDAPValidationResult> results);

  void removeGroups();

  Set<RDAPValidationResult> getAll();

  boolean isEmpty();

  Set<String> getGroupOk();

  void addGroups(Set<String> groups);

  Set<String> getGroupErrorWarning();

  void addGroup(String group);

  void addGroupErrorWarning(String group);

  Set<String> getGroups();

  void clear();

  String analyzeResultsWithStatusCheck();

  void cullDuplicateIPAddressErrors();
}
