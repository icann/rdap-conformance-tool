package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.net.URI;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseValidation2Dot7Dot5Dot2 extends EntitiesWithinDomainProfileJsonValidation {

  public ResponseValidation2Dot7Dot5Dot2(String rdapResponse,
      RDAPValidatorResults results,
      RDAPQueryType queryType,
      RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_7_5_2_Validation";
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    boolean containsEmail = false;
    String contactUri = null;
    JSONArray vcardArray = entity.getJSONArray("vcardArray");
    for (Object vcardElement : vcardArray) {
      if (vcardElement instanceof JSONArray) {
        JSONArray vcardElementArray = (JSONArray) vcardElement;
        for (Object categoryArray : vcardElementArray) {
          JSONArray categoryJsonArray = ((JSONArray) categoryArray);
          String category = categoryJsonArray.getString(0);
          if (category.equals("email")) {
            containsEmail = true;
          }
          if (category.equals("contact-uri")) {
            Object value = categoryJsonArray.get(3);
            if (value instanceof String) {
              contactUri = (String) value;
            }
          }
        }
      }
    }
    if (!containsEmail) {
      if (null == contactUri) {
        results.add(RDAPValidationResult.builder()
            .code(-58000)
            .value(getResultValue(jsonPointer))
            .message(("An entity with the administrative, technical, or billing role without a "
                + ("CONTACT-URI member was found. "
                + "See section 2.7.5.2 of the RDAP_Response_Profile_2_1.")))
            .build());
        return false;
      } else if (!contactUri.contains("@")
          && (null == URI.create(contactUri).getScheme()
          || !Set.of("http", "https").contains(URI.create(contactUri).getScheme()))) {
        results.add(RDAPValidationResult.builder()
            .code(-58001)
            .value(getResultValue(jsonPointer))
            .message(("The content of the CONTACT-URI member of an entity with the administrative, "
                + "technical, or billing role does not contain an email or http/https link. "
                + "See section 2.7.5.2 of the RDAP_Response_Profile_2_1."))
            .build());
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN) && config.isGtldRegistrar();
  }
}
