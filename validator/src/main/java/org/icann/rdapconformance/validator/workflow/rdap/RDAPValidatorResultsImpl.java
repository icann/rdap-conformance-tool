package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


public class RDAPValidatorResultsImpl implements RDAPValidatorResults {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidatorResultsImpl.class);
  public static final String CODE = "code=";
  public static final String HTTP_STATUS_CODE = ", httpStatusCode=";
  public static final String BRACKETS = "[]";
  public static final List<Integer> falsePositivesCodesForIpvPrivateCheck = Arrays.asList(12208, 12407);

  private final List<RDAPValidationResult> results = new ArrayList<>();
  private final Set<String> groups = ConcurrentHashMap.newKeySet();
  private final Set<String> groupErrorWarning = ConcurrentHashMap.newKeySet();

  // Public constructor for QueryContext usage
  public RDAPValidatorResultsImpl() {}


  public int getResultCount() {
    return results.size();
  }

  @Override
  public void add(RDAPValidationResult result) {
    this.results.add(result);
    logger.debug("adding error result {}", result);
  }

  @Override
  public void addAll(Set<RDAPValidationResult> results) {
    this.results.clear();
    this.results.addAll(results);
  }

  @Override
  public void removeGroups() {
    this.groups.clear();
    this.groupErrorWarning.clear();
  }

  @Override
  public Set<RDAPValidationResult> getAll() {
    return new HashSet<>(results);
  }

  @Override
  public List<RDAPValidationResult> getAllAsList() {
    return new ArrayList<>(results);
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
   *  Unused but keep for debugging purposes
   */
  public String prettyPrintResults() {
    StringBuilder sb = new StringBuilder();
    for (RDAPValidationResult result : results) {
      sb.append(result.toString()).append(System.lineSeparator());
    }
    return sb.toString();
  }

  public String analyzeResultsWithStatusCheck() {
    StringBuilder sb = new StringBuilder();
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
      logger.debug("Not all status codes are the same");
      results.add(
          new RDAPValidationResult(
              -13018,
              tupleListJson,
              "Queries do not produce the same HTTP status code.",
              DASH,
              DASH,
              DASH,
              ZERO,
              DASH
          )
      );
    } else {
      logger.debug("All status codes are the same");
    }

    // Return a Pretty Printed and filtered results
    for (RDAPValidationResult result : filtered) {
      sb.append(CODE).append(result.getCode())
        .append(HTTP_STATUS_CODE).append(result.getHttpStatusCode())
        .append(System.lineSeparator());
    }
    return sb.toString();
  }

  // New culling function
  public void cullDuplicateIPAddressErrors() {
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

}