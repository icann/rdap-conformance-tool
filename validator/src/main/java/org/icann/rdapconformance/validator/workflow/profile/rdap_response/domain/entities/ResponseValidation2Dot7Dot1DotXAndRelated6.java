package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 8.8.1.6
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated6 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

  private final QueryContext queryContext;

  public ResponseValidation2Dot7Dot1DotXAndRelated6(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults(), queryContext.getQueryType(), queryContext.getConfig());
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use ResponseValidation2Dot7Dot1DotXAndRelated6(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public ResponseValidation2Dot7Dot1DotXAndRelated6(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType, RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
    this.queryContext = null; // For testing purposes only
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    if (!getPointerFromJPath(entity, "[?(@.roles contains 'registrant')]").isEmpty()) {
      Set<String> adrPointers = getPointerFromJPath(entity, "vcardArray[1][?(@[0] == 'adr')]");
      for (String adrPointer : adrPointers) {
        JSONArray adr = (JSONArray) entity.query(adrPointer);
        if (!adr.getJSONObject(1).has("cc")) {
          RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
              .code(-52105)
              .value(getResultValue(jsonPointer))
              .message("An entity with the registrant role without the CC parameter "
                  + "was found. See section 2.7.3.1 of the RDAP_Response_Profile_2_1.");

          results.add(builder.build(queryContext));
          return false;
        }
      }
    }

    return true;
  }
}
