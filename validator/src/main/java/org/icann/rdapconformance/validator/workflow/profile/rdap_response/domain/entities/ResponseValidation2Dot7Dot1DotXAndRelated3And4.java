package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.util.Set;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

/**
 * 8.8.1.3 & 8.8.1.4
 */
public class ResponseValidation2Dot7Dot1DotXAndRelated3And4 extends
    ResponseValidation2Dot7Dot1DotXAndRelated {

  private final SimpleHandleValidation simpleHandleValidation;

  public ResponseValidation2Dot7Dot1DotXAndRelated3And4(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults(), queryContext.getQueryType(), queryContext.getConfig());
    this.simpleHandleValidation = new SimpleHandleValidation(queryContext, -52102);
  }

  /**
   * @deprecated Use ResponseValidation2Dot7Dot1DotXAndRelated3And4(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public ResponseValidation2Dot7Dot1DotXAndRelated3And4(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType, RDAPValidatorConfiguration config, SimpleHandleValidation simpleHandleValidation) {
    super(rdapResponse, results, queryType, config);
    this.simpleHandleValidation = simpleHandleValidation;
  }

  @Override
  protected boolean doValidateEntity(String jsonPointer, JSONObject entity) {
    if (isChildOfRegistrar(jsonPointer)) {
      return true;
    }

    Set<String> withRemarkTitleRedactedForPrivacy =
        getPointerFromJPath(entity, "$.remarks[?(@.title == 'REDACTED FOR PRIVACY')]");

    if (withRemarkTitleRedactedForPrivacy.isEmpty()) {
      return simpleHandleValidation.validateHandle(jsonPointer + "/handle");
    }

    return true;
  }
}
