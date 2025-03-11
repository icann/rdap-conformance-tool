package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.json.JSONArray;
import org.json.JSONObject;

public final class TigValidation3Dot2 extends ProfileJsonValidation {

  private final RDAPValidatorConfiguration config;
  private final RDAPQueryType queryType;
  private final RDAPDatasetService datasetService;

  public TigValidation3Dot2(String rdapResponse, RDAPValidatorResults results,
      RDAPValidatorConfiguration config,
      RDAPDatasetService datasetService,
      RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.config = config;
    this.datasetService = datasetService;
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "tigSection_3_2_Validation";
  }

  @Override
  public boolean doValidate() {
    boolean isValid = false;
    JSONArray links = jsonObject.optJSONArray("links");
    if (links != null) {
      for (Object link : links) {
        JSONObject l = (JSONObject) link;
        if (l.optString("rel").equals("related") && l.optString("href", null) != null) {
          isValid = true;
        }
      }
    }

    if (!isValid && !isGtldRegistryAndNotRegistrarId9999()) {
      String linksStr = links == null ? "" : links.toString();
      results.add(RDAPValidationResult.builder()
          .code(-23200)
          .value(linksStr)
          .message("A links data structure in the topmost object exists, and the links object "
              + "shall contain the elements rel:related and href, but they were not found. "
              + "See section 3.2 of the RDAP_Technical_Implementation_Guide_2_1.")
          .build());
    }
    return isValid;
  }

  // RCT-104 only apply if the query is for a gtld registry and the value is not 9999
  public boolean isGtldRegistryAndNotRegistrarId9999() {
    RegistrarId registrarId = datasetService.get(RegistrarId.class);
    return config.isGtldRegistry() && registrarId.containsId(9999);
  }

  @Override
  public boolean doLaunch() {
    return config.isGtldRegistry() && queryType.equals(RDAPQueryType.DOMAIN);
  }

}
