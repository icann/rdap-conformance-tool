package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public class ResponseValidation2Dot7Dot5Dot3 extends EntitiesWithinDomainProfileJsonValidation {

  public ResponseValidation2Dot7Dot5Dot3(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults(), queryContext.getQueryType(), queryContext.getConfig());
  }

  /**
   * @deprecated Use ResponseValidation2Dot7Dot5Dot3(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public ResponseValidation2Dot7Dot5Dot3(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType, RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_2_7_5_3_Validation";
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    boolean emailOmitted = isEmailOmitted(entity);
    if (emailOmitted && getPointerFromJPath(entity,
        "$.remarks[?(@.title == 'EMAIL REDACTED FOR PRIVACY' && "
            + "@.type == 'object redacted due to authorization')]").isEmpty()) {
      results.add(RDAPValidationResult.builder()
          .code(-55000)
          .value(getResultValue(jsonPointer))
          .message("An entity with the administrative, technical, or billing role "
              + "without a valid \"EMAIL REDACTED FOR PRIVACY\" remark was found. See section 2.7.5.3 "
              + "of the RDAP_Response_Profile_2_1.")
          .build());
      return false;
    }
    return true;
  }

  private boolean isEmailOmitted(JSONObject entity) {
    JSONArray vcardArray = entity.getJSONArray("vcardArray");
    for (Object vcardElement : vcardArray) {
      if (vcardElement instanceof JSONArray) {
        JSONArray vcardElementArray = (JSONArray) vcardElement;
        for (Object categoryArray : vcardElementArray) {
          JSONArray categoryJsonArray = ((JSONArray) categoryArray);
          String category = categoryJsonArray.getString(0);
          if (category.equals("email")) {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN) && !config.isThin() && config.isGtldRegistry();
  }
}
