package org.icann.rdapconformance.validator;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.ParseContext;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.schema.JsonPointers;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpathUtil {
  private static final Logger logger = LoggerFactory.getLogger(JpathUtil.class);

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

  /**
   * This is meant to check the syntax error of jsonPath only.
   *
   * TODO: JsonPath.compile(path) is being used to change if a given jsonpath is valid.
   * But some invalid path will pass this check. Might need to revisit to find a more accurate way
   * to do the validation. As of June 2025, it seems that there is no easy perfect way on the market.
   */
  public boolean isValidJsonPath(String jsonPath) {
    try {
      JsonPath.compile(jsonPath);
    } catch (Exception e) {
      logger.debug("Invalid JSON path: {} with error: {}", jsonPath, e.getMessage());
      return false;
    }

    return true;
  }
}
