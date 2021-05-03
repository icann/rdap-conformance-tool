package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class Validation3Dot2 {

  private final String rdapResponse;
  private final RDAPValidatorResults results;
  private final RDAPDatasetService datasetService;

  public Validation3Dot2(String rdapResponse,
      RDAPValidatorResults results,
      RDAPDatasetService datasetService) {
    this.rdapResponse = rdapResponse;
    this.results = results;
    this.datasetService = datasetService;
  }

  public boolean validate() {
    boolean hasError = false;

    SchemaValidator validator = new SchemaValidator("profile/tig_section/topmost_object.json",
        new RDAPValidatorResults(), datasetService);

    if (!validator.validate(rdapResponse)) {
      JSONObject rdapResponseJson = new JSONObject(rdapResponse);
      JSONArray links = rdapResponseJson.optJSONArray("links");
      String linksStr = links == null ? "" : links.toString();
      results.add(RDAPValidationResult.builder()
          .code(-23200)
          .value(linksStr)
          .message("A links data structure in the topmost object exists, and the links object "
              + "shall contain the elements rel:related and href, but they were not found. "
              + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.")
          .build());
      hasError = true;
    }
    results.addGroup("tigSection_3_2_Validation", hasError);
    return !hasError;
  }
}
