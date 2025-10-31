package org.icann.rdapconformance.validator.workflow.profile.rdap_response.entity;

import java.util.HashSet;
import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ResponseValidation3Dot2 extends ProfileJsonValidation {

  protected final RDAPValidatorConfiguration config;
  private final RDAPQueryType queryType;
  private final QueryContext queryContext;

  public ResponseValidation3Dot2(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.queryType = queryContext.getQueryType();
    this.config = queryContext.getConfig();
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use ResponseValidation3Dot2(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public ResponseValidation3Dot2(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType, RDAPValidatorConfiguration config) {
    super(rdapResponse, results);
    this.queryType = queryType;
    this.config = config;
    this.queryContext = null; // Not available in deprecated constructor
  }


  @Override
  public String getGroupName() {
    return "rdapResponseProfile_3_2_Validation";
  }

  public boolean doValidate() {
    boolean isValid = true;

    Set<String> entitiesJsonPointers = getPointerFromJPath(
        "$.entities[?(@.roles contains 'administrative' || @.roles contains 'technical')]");

    for (String entityJsonPointer : entitiesJsonPointers) {
      JSONObject entity = (JSONObject) jsonObject.query(entityJsonPointer);
      Set<String> vcardJsonPointers = getPointerFromJPath(entity, "$.vcardArray");

      for (String jsonPointer : vcardJsonPointers) {
        if (!checkVcard(entityJsonPointer.concat(jsonPointer.substring(1)))) {
          RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
              .code(-60200)
              .value(getResultValue(entityJsonPointer))
              .message(
                  "The required members for entities with the administrative and technical roles "
                      + "were not found. See section 3.2 of the RDAP_Response_Profile_2_1.");

          if (queryContext != null) {
            results.add(builder.build(queryContext));
          } else {
            results.add(builder.build()); // Fallback for deprecated constructor
          }
          isValid = false;
        }
      }
    }

    return isValid;
  }

  private boolean checkVcard(String vcardJsonPointer) {
    Set<String> requiredMembers = new HashSet<>(Set.of("fn", "tel", "email"));
    JSONArray vcardArray = (JSONArray) jsonObject.query(vcardJsonPointer);
    for (Object vcardElement : vcardArray) {
      if (vcardElement instanceof JSONArray) {
        JSONArray vcardElementArray = (JSONArray) vcardElement;
        for (Object categoryArray : vcardElementArray) {
          JSONArray categoryJsonArray = ((JSONArray) categoryArray);
          String category = categoryJsonArray.getString(0);
          requiredMembers.remove(category);
        }
      }
    }
    return requiredMembers.isEmpty();
  }


  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.ENTITY) && config.isGtldRegistry();
  }
}
