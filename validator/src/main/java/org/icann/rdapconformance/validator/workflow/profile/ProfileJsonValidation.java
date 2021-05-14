package org.icann.rdapconformance.validator.workflow.profile;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public abstract class ProfileJsonValidation extends ProfileValidation {

  protected final JSONObject jsonObject;

  public ProfileJsonValidation(String rdapResponse, RDAPValidatorResults results) {
    super(results);
    jsonObject = new JSONObject(rdapResponse);
  }

  public DocumentContext getJPath() {
    return getJPath(jsonObject);
  }

  private DocumentContext getJPath(JSONObject json) {
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    return using(jsonPathConfig).parse(json.toString());
  }

  protected boolean exists(String jpath) {
    return !getPointerFromJPath(jsonObject, jpath).isEmpty();
  }

  protected Set<String> getPointerFromJPath(String jpath) {
    return getPointerFromJPath(jsonObject, jpath);
  }

  protected Set<String> getPointerFromJPath(JSONObject entity, String jpath) {
    List<String> jpaths = getJPath(entity).read(jpath);
    return jpaths
        .stream()
        .map(JsonPointers::fromJpath)
        .collect(Collectors.toSet());
  }

  public String getResultValue(String jsonPointer) {
    return jsonPointer + ":" + jsonObject.query(jsonPointer);
  }

  public String getResultValue(Set<String> jsonPointers) {
    return jsonPointers.stream()
        .map(this::getResultValue)
        .collect(Collectors.joining(", "));
  }
}
