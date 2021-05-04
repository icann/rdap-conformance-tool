package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class Validation3Dot2 {

  private final String rdapResponse;
  private final RDAPValidatorResults results;

  public Validation3Dot2(String rdapResponse, RDAPValidatorResults results) {
    this.rdapResponse = rdapResponse;
    this.results = results;
  }

  public boolean validate() {
    boolean hasError = true;

    JSONObject rdapResponseJson = new JSONObject(rdapResponse);
    JSONArray links = rdapResponseJson.optJSONArray("links");
    if (links != null) {
      for (Object link : links) {
        JSONObject l = (JSONObject) link;
        if (l.optString("rel").equals("related") && l.optString("href", null) != null) {
          hasError = false;
        }
      }
    }

    if (hasError) {
      String linksStr = links == null ? "" : links.toString();
      results.add(RDAPValidationResult.builder()
          .code(-23200)
          .value(linksStr)
          .message("A links data structure in the topmost object exists, and the links object "
              + "shall contain the elements rel:related and href, but they were not found. "
              + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.")
          .build());
    }
    results.addGroup("tigSection_3_2_Validation", hasError);
    return !hasError;
  }
}
