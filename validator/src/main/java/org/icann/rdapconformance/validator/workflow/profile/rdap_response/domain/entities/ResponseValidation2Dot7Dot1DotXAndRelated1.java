package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * 8.8.1.1
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated1 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

  private final QueryContext queryContext;

  public ResponseValidation2Dot7Dot1DotXAndRelated1(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults(), queryContext.getQueryType(), queryContext.getConfig());
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use ResponseValidation2Dot7Dot1DotXAndRelated1(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public ResponseValidation2Dot7Dot1DotXAndRelated1(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType, RDAPValidatorConfiguration config) {
    super(rdapResponse, results, queryType, config);
    this.queryContext = null; // Not available in deprecated constructor
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    Set<String> withRemarkTitleRedactedForPrivacy =
        getPointerFromJPath(entity, "$.remarks[?(@.title == 'REDACTED FOR PRIVACY')]");

    for (String remarkJsonPointer : withRemarkTitleRedactedForPrivacy) {
      JSONObject remark = (JSONObject) entity.query(remarkJsonPointer);
      if (!remark.has("type") || !remark.get("type").equals("object redacted due to "
          + "authorization")) {
        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
            .code(-52100)
            .value(getResultValue(jsonPointer))
            .message("An entity with the registrant, administrative, technical or "
                + "billing role with a remarks members with the title \"REDACTED FOR PRIVACY\" was "
                + "found, but the description and type does not contain the value in 2.7.4.3 of "
                + "the "
                + "RDAP_Response_Profile_2_1.");

        results.add(builder.build(queryContext));
        return false;
      }
    }
    return true;
  }
}
