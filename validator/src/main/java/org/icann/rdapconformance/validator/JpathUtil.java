package org.icann.rdapconformance.validator;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.json.JSONObject;

public class JpathUtil {

  private final ParseContext parseContext;

  public JpathUtil() {
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    parseContext = using(jsonPathConfig);
  }

  public boolean exists(JSONObject jsonObject, String jpath) {
    return exists(jsonObject.toString(), jpath);
  }

  public boolean exists(String json, String jpath) {
    return !getPointerFromJPath(json, jpath).isEmpty();
  }

  public Set<String> getPointerFromJPath(JSONObject jsonObject, String jpath) {
    return getPointerFromJPath(jsonObject.toString(), jpath);
  }

  public Set<String> getPointerFromJPath(String json, String jpath) {
    List<String> jpaths = parseContext.parse(json).read(jpath);
    return jpaths
        .stream()
        .map(JsonPointers::fromJpath)
        .collect(Collectors.toSet());
  }
}
