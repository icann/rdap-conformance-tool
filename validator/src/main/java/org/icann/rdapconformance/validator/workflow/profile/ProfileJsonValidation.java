package org.icann.rdapconformance.validator.workflow.profile;

import java.util.Set;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.JpathUtil;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public abstract class ProfileJsonValidation extends ProfileValidation {

  protected final JSONObject jsonObject;
  private final JpathUtil jpathUtil;

  public ProfileJsonValidation(String rdapResponse, RDAPValidatorResults results) {
    super(results);
    // Use cached JSON parsing to avoid repeated parsing of the same RDAP response
    jsonObject = org.icann.rdapconformance.validator.workflow.JsonCacheUtil.getCachedJsonObject(rdapResponse);
    jpathUtil = new JpathUtil(); // ready to dependency injection if needed sometimes
  }

  protected boolean exists(String jpath) {
    return jpathUtil.exists(jsonObject, jpath);
  }

  protected Set<String> getPointerFromJPath(String jpath) {
    return getPointerFromJPath(jsonObject, jpath);
  }

  protected Set<String> getPointerFromJPath(JSONObject entity, String jpath) {
    return jpathUtil.getPointerFromJPath(entity, jpath);
  }

  public String getResultValue(String jsonPointer) {
    return jsonPointer + ":" + jsonObject.query(jsonPointer);
  }

  public String getResultValue(Set<String> jsonPointers) {
    return jsonPointers.stream()
        .map(this::getResultValue)
        .sorted()
        .collect(Collectors.joining(", "));
  }

  public boolean isValidJsonPath(String jsonPath) {
    return jpathUtil.isValidJsonPath(jsonPath);
  }
}
