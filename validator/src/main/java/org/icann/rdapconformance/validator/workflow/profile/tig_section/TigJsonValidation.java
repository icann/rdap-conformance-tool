package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import static com.jayway.jsonpath.JsonPath.using;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public abstract class TigJsonValidation extends TigValidation {

  protected final String rdapResponse;
  protected final JSONObject jsonObject;

  public TigJsonValidation(String rdapResponse, RDAPValidatorResults results) {
    super(results);
    this.rdapResponse = rdapResponse;
    jsonObject = new JSONObject(rdapResponse);
  }

  public DocumentContext getJPath() {
    Configuration jsonPathConfig = Configuration.defaultConfiguration()
        .addOptions(Option.AS_PATH_LIST)
        .addOptions(Option.SUPPRESS_EXCEPTIONS);
    return using(jsonPathConfig).parse(rdapResponse);
  }
}
