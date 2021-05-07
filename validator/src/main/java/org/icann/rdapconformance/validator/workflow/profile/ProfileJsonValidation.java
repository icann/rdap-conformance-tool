package org.icann.rdapconformance.validator.workflow.profile;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.Option;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public abstract class ProfileJsonValidation extends ProfileValidation {

  protected final JSONObject jsonObject;

  public ProfileJsonValidation(String rdapResponse, RDAPValidatorResults results) {
    super(results);
    jsonObject = new JSONObject(rdapResponse);
  }

  public DocumentContext getJPath() {
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    return using(jsonPathConfig).parse(jsonObject.toString());
  }

  public String getResultValue(String jsonPointer) {
    return jsonPointer + ":" + jsonObject.query(jsonPointer);
  }
}
