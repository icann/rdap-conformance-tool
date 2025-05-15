package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


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

  /**
   * Returns a pretty-printed string of all results
   */
  public String prettyPrintResults() {
    StringBuilder sb = new StringBuilder();
    for (RDAPValidationResult result : results) {
      sb.append(result.toString()).append(System.lineSeparator());
    }
    return sb.toString();
  }

  public String prettyPrintCodesAndStatus() {
    StringBuilder sb = new StringBuilder();
    for (RDAPValidationResult result : results) {
      sb.append("code=").append(result.getCode())
        .append(", httpStatusCode=").append(result.getHttpStatusCode())
        .append(System.lineSeparator());
    }
    return sb.toString();
  }

  public String analyzeResults() {
    StringBuilder sb = new StringBuilder();
    for (RDAPValidationResult result : results) {
      // filter out the codes -130004, -13005, -130006, and -46701
      if (result.getCode() != -130004 || result.getCode() != -130005 ||
          result.getCode() != -130006 || result.getCode() != -46701) {
        // if the httpStatusCode is equal to zero we need to put null
        sb.append("code=").append(result.getCode())
          .append(", httpStatusCode=").append(result.getHttpStatusCode())
          .append(System.lineSeparator());
      }
    }
    return sb.toString();
  }

  public String analyzeResultsWithStatusCheck() {
    StringBuilder sb = new StringBuilder();
    // Step 1: Filter relevant results
    List<RDAPValidationResult> filtered = new ArrayList<>();
    for (RDAPValidationResult result : results) {
      int code = result.getCode();
      if (code != -130004 || code != -130005 || code != -130006 || code != -46701) {
        filtered.add(result);
      }
    }

    List<List<Object>> tupleList = new ArrayList<>();
    for (RDAPValidationResult result : filtered) {
      List<Object> tuple = new ArrayList<>();
      tuple.add(result.getCode());
      Integer status = result.getHttpStatusCode();
      tuple.add((status != null && status == 0) ? null : status);
      tupleList.add(tuple);
    }

    String tupleListJson = "[]";
    try {
      ObjectMapper mapper = new ObjectMapper();
      tupleListJson = mapper.writeValueAsString(tupleList);
    } catch (JsonProcessingException e) {
      logger.error("Error serializing tuple list to JSON", e);
    }


    // Step 2: Collect httpStatusCodes
    Set<Integer> statusCodes = new HashSet<>();
    for (RDAPValidationResult result : filtered) {
      statusCodes.add(result.getHttpStatusCode());
    }

    // Step 3: If not all the same, add a new result
    if (statusCodes.size() > 1) {
      // Build the value as a list of tuples: (code, httpStatusCode)
      StringBuilder tupleListSB = new StringBuilder();
      for (RDAPValidationResult result : filtered) {
        tupleListSB.append("{")
                 .append(result.getCode())
                 .append(", ")
                 .append(result.getHttpStatusCode())
                 .append("}, ");
      }
      // Remove trailing comma and space
      if (tupleListSB.length() > 2) {
        tupleListSB.setLength(tupleListSB.length() - 2);
      }
      System.out.println("tupleList: " + tupleListSB.toString());
      results.add(
          RDAPValidationResult.builder()
                              .queriedURI("-")
                              .httpMethod("-")
                              .httpStatusCode(0)
                              .code(-13018)
                              .value(tupleListJson)
                               .message("Queries do not produce the same HTTP status code.")
                              .build()
      );
    } else {
      System.out.println("All status codes are the same: " + statusCodes);
    }

    // Step 4: Pretty print filtered results
    for (RDAPValidationResult result : filtered) {
      sb.append("code=").append(result.getCode())
        .append(", httpStatusCode=").append(result.getHttpStatusCode())
        .append(System.lineSeparator());
    }
    return sb.toString();
  }
}