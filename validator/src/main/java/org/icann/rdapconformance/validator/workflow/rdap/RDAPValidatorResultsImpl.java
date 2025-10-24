package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


public class RDAPValidatorResultsImpl implements RDAPValidatorResults {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidatorResultsImpl.class);
  public static final String CODE = "code=";
  public static final String HTTP_STATUS_CODE = ", httpStatusCode=";
  public static final String BRACKETS = "[]";

  // Session-keyed storage for concurrent validation requests
  private static final ConcurrentHashMap<String, RDAPValidatorResultsImpl> sessionInstances = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Set<RDAPValidationResult>> sessionResults = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Set<String>> sessionGroups = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Set<String>> sessionGroupErrorWarning = new ConcurrentHashMap<>();

  // Instance holds its session ID for accessing the correct session data
  private final String sessionId;

  // Private constructor to prevent external instantiation
  private RDAPValidatorResultsImpl(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * Gets the singleton instance for a specific session
   *
   * @param sessionId the session identifier
   * @return the singleton instance for this session
   */
  public static RDAPValidatorResultsImpl getInstance(String sessionId) {
    return sessionInstances.computeIfAbsent(sessionId, RDAPValidatorResultsImpl::createNewInstance);
  }

  /**
   * Creates a new instance and initializes session data atomically.
   * This method is called by computeIfAbsent() which ensures thread-safety.
   *
   * @param sessionId the session identifier
   * @return a new RDAPValidatorResultsImpl instance
   */
  private static RDAPValidatorResultsImpl createNewInstance(String sessionId) {
    // Initialize session data when creating new instance
    // ConcurrentHashMap.computeIfAbsent() ensures this initialization is atomic
    sessionResults.put(sessionId, ConcurrentHashMap.newKeySet());
    sessionGroups.put(sessionId, ConcurrentHashMap.newKeySet());
    sessionGroupErrorWarning.put(sessionId, ConcurrentHashMap.newKeySet());
    return new RDAPValidatorResultsImpl(sessionId);
  }


  /**
   * Resets the singleton instance for a specific session
   *
   * @param sessionId the session to reset
   */
  public static void reset(String sessionId) {
    sessionInstances.remove(sessionId);
    sessionResults.remove(sessionId);
    sessionGroups.remove(sessionId);
    sessionGroupErrorWarning.remove(sessionId);
  }

  /**
   * Resets all sessions (primarily for testing)
   */
  public static void resetAll() {
    sessionInstances.clear();
    sessionResults.clear();
    sessionGroups.clear();
    sessionGroupErrorWarning.clear();
  }


  /**
   * Gets the results count for a specific session
   *
   * @param sessionId the session identifier
   * @return the number of results for this session
   */
  public int getResultCount(String sessionId) {
    Set<RDAPValidationResult> results = sessionResults.get(sessionId);
    return results != null ? results.size() : 0;
  }


  /**
   * Adds a validation result to a specific session
   *
   * @param sessionId the session identifier
   * @param result the validation result to add
   */
  public void add(String sessionId, RDAPValidationResult result) {
    Set<RDAPValidationResult> results = sessionResults.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet());
    if (results.add(result)) {
      logger.debug("adding error result {} for session {}", result, sessionId);
    }
  }

  @Override
  public void add(RDAPValidationResult result) {
    add(this.sessionId, result);
  }

  /**
   * Replaces all results for a specific session atomically
   *
   * @param sessionId the session identifier
   * @param results the new results set
   */
  public void addAll(String sessionId, Set<RDAPValidationResult> results) {
    // Create a new concurrent set with the provided results to ensure atomicity
    // This avoids the race condition between clear() and addAll() operations
    Set<RDAPValidationResult> newResultsSet = ConcurrentHashMap.newKeySet();
    newResultsSet.addAll(results);
    sessionResults.put(sessionId, newResultsSet);
  }

  @Override
  public void addAll(Set<RDAPValidationResult> results) {
    addAll(this.sessionId, results);
  }

  /**
   * Removes all groups for a specific session
   *
   * @param sessionId the session identifier
   */
  public void removeGroups(String sessionId) {
    Set<String> groups = sessionGroups.get(sessionId);
    Set<String> groupErrorWarning = sessionGroupErrorWarning.get(sessionId);
    if (groups != null) {
      groups.clear();
    }
    if (groupErrorWarning != null) {
      groupErrorWarning.clear();
    }
  }

  @Override
  public void removeGroups() {
    removeGroups(this.sessionId);
  }

  /**
   * Gets all results for a specific session
   *
   * @param sessionId the session identifier
   * @return the results set for this session
   */
  public Set<RDAPValidationResult> getAll(String sessionId) {
    return sessionResults.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
  }

  @Override
  public Set<RDAPValidationResult> getAll() {
    return getAll(this.sessionId);
  }

  /**
   * Checks if results are empty for a specific session
   *
   * @param sessionId the session identifier
   * @return true if no results exist for this session
   */
  public boolean isEmpty(String sessionId) {
    Set<RDAPValidationResult> results = sessionResults.get(sessionId);
    return results == null || results.isEmpty();
  }

  @Override
  public boolean isEmpty() {
    return isEmpty(this.sessionId);
  }

  /**
   * Gets the OK groups for a specific session (groups without errors/warnings)
   *
   * @param sessionId the session identifier
   * @return the set of OK groups for this session
   */
  public Set<String> getGroupOk(String sessionId) {
    Set<String> groups = sessionGroups.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
    Set<String> groupErrorWarning = sessionGroupErrorWarning.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
    Set<String> groupsCopy = new HashSet<>(groups);
    groupsCopy.removeAll(groupErrorWarning);
    return groupsCopy;
  }

  @Override
  public Set<String> getGroupOk() {
    return getGroupOk(this.sessionId);
  }

  /**
   * Adds multiple groups to a specific session
   *
   * @param sessionId the session identifier
   * @param groups the groups to add
   */
  public void addGroups(String sessionId, Set<String> groups) {
    Set<String> sessionGroupsSet = sessionGroups.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet());
    sessionGroupsSet.addAll(groups);
  }

  @Override
  public void addGroups(Set<String> groups) {
    addGroups(this.sessionId, groups);
  }

  /**
   * Gets the error/warning groups for a specific session
   *
   * @param sessionId the session identifier
   * @return the set of error/warning groups for this session
   */
  public Set<String> getGroupErrorWarning(String sessionId) {
    return sessionGroupErrorWarning.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
  }

  @Override
  public Set<String> getGroupErrorWarning() {
    return getGroupErrorWarning(this.sessionId);
  }

  /**
   * Adds a single group to a specific session
   *
   * @param sessionId the session identifier
   * @param group the group to add
   */
  public void addGroup(String sessionId, String group) {
    Set<String> groups = sessionGroups.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet());
    groups.add(group);
  }

  @Override
  public void addGroup(String group) {
    addGroup(this.sessionId, group);
  }

  /**
   * Adds a group as an error/warning group to a specific session
   *
   * @param sessionId the session identifier
   * @param group the group to add as error/warning
   */
  public void addGroupErrorWarning(String sessionId, String group) {
    this.addGroup(sessionId, group);
    Set<String> groupErrorWarning = sessionGroupErrorWarning.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet());
    groupErrorWarning.add(group);
  }

  @Override
  public void addGroupErrorWarning(String group) {
    addGroupErrorWarning(this.sessionId, group);
  }

  /**
   * Gets all groups for a specific session
   *
   * @param sessionId the session identifier
   * @return the set of all groups for this session
   */
  public Set<String> getGroups(String sessionId) {
    return sessionGroups.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
  }

  @Override
  public Set<String> getGroups() {
    return getGroups(this.sessionId);
  }

  /**
   * Clears all results and groups for a specific session
   *
   * @param sessionId the session to clear
   */
  public void clear(String sessionId) {
    Set<RDAPValidationResult> results = sessionResults.get(sessionId);
    Set<String> groups = sessionGroups.get(sessionId);
    Set<String> groupErrorWarning = sessionGroupErrorWarning.get(sessionId);

    if (results != null) {
      results.clear();
    }
    if (groups != null) {
      groups.clear();
    }
    if (groupErrorWarning != null) {
      groupErrorWarning.clear();
    }
  }

  @Override
  public void clear() {
    clear(this.sessionId);
  }


  /**
   * Returns a pretty-printed string of all results for a specific session
   *
   * @param sessionId the session identifier
   * @return formatted string of results for debugging
   */
  public String prettyPrintResults(String sessionId) {
    StringBuilder sb = new StringBuilder();
    Set<RDAPValidationResult> results = sessionResults.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
    for (RDAPValidationResult result : results) {
      sb.append(result.toString()).append(System.lineSeparator());
    }
    return sb.toString();
  }


  /**
   * Analyzes results with status check for a specific session
   *
   * @param sessionId the session identifier
   * @return analysis string
   */
  public String analyzeResultsWithStatusCheck(String sessionId) {
    StringBuilder sb = new StringBuilder();
    Set<RDAPValidationResult> results = sessionResults.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());

    // Filter relevant results
    List<RDAPValidationResult> filtered = new ArrayList<>();
    for (RDAPValidationResult result : results) {
      int code = result.getCode();
      if (code != -130004 && code != -130005 && code != -130006 && code != -65300) {
        filtered.add(result);
      }
    }

    Set<List<Object>> uniqueTuples = new HashSet<>();  // Use Set to ensure uniqueness
    for (RDAPValidationResult result : filtered) {
      List<Object> tuple = new ArrayList<>();
      tuple.add(result.getCode());
      Integer status = result.getHttpStatusCode();
      tuple.add(status == null ? ZERO : status);
      uniqueTuples.add(tuple);
    }

    String tupleListJson = BRACKETS;
    try {
      ObjectMapper mapper = org.icann.rdapconformance.validator.workflow.JsonMapperUtil.getSharedMapper();
      tupleListJson = mapper.writeValueAsString(new ArrayList<>(uniqueTuples));
    } catch (JsonProcessingException e) {
      logger.debug("Error serializing tuple list to JSON", e);
    }

    // Collect httpStatusCodes - normalize null to 0
    Set<Integer> statusCodes = new HashSet<>();
    for (RDAPValidationResult result : filtered) {
      Integer statusCode = result.getHttpStatusCode();
      statusCodes.add(statusCode == null ? ZERO : statusCode);
    }

    // If not all the same, add the new error code
    if (statusCodes.size() > ONE) {
      logger.debug("Not all status codes are the same for session {}", sessionId);
      results.add(
          RDAPValidationResult.builder(sessionId)
                              .acceptHeader(DASH)
                              .queriedURI(DASH)
                              .httpMethod(DASH)
                              .httpStatusCode(ZERO)
                              .code(-13018)
                              .value(tupleListJson)
                               .message("Queries do not produce the same HTTP status code.")
                              .build()
      );
    } else {
      logger.debug("All status codes are the same for session {}", sessionId);
    }

    // Return a Pretty Printed and filtered results
    for (RDAPValidationResult result : filtered) {
      sb.append(CODE).append(result.getCode())
        .append(HTTP_STATUS_CODE).append(result.getHttpStatusCode())
        .append(System.lineSeparator());
    }
    return sb.toString();
  }


  /**
   * Culls duplicate IP address errors for a specific session
   *
   * @param sessionId the session identifier
   */
  public void cullDuplicateIPAddressErrors(String sessionId) {
    Set<RDAPValidationResult> results = sessionResults.getOrDefault(sessionId, ConcurrentHashMap.newKeySet());
    int ipv4Count = ZERO;
    int ipv6Count = ZERO;
    Set<RDAPValidationResult> toRemove = new HashSet<>();

    // First pass - count occurrences
    for (RDAPValidationResult result : results) {
      if (result.getCode() == -20400) {
        ipv4Count++;
        if (ipv4Count > ONE) {
          toRemove.add(result);
        }
      } else if (result.getCode() == -20401) {
        ipv6Count++;
        if (ipv6Count > ONE) {
          toRemove.add(result);
        }
      }
    }

    // Remove duplicates
    results.removeAll(toRemove);
  }

  @Override
  public void cullDuplicateIPAddressErrors() {
    cullDuplicateIPAddressErrors(this.sessionId);
  }

  @Override
  public String analyzeResultsWithStatusCheck() {
    return analyzeResultsWithStatusCheck(this.sessionId);
  }

  @Override
  public int getResultCount() {
    return getResultCount(this.sessionId);
  }

  @Override
  public String getSessionId() {
    return this.sessionId;
  }

}