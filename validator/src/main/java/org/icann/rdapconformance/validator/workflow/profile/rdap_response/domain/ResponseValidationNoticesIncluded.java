package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

public final class ResponseValidationNoticesIncluded extends ProfileJsonValidation {

  private final RDAPQueryType queryType;
  private final QueryContext queryContext;

  public ResponseValidationNoticesIncluded(QueryContext queryContext) {
    super(queryContext.getRdapResponseData(), queryContext.getResults());
    this.queryType = queryContext.getQueryType();
    this.queryContext = queryContext;
  }

  /**
   * @deprecated Use ResponseValidationNoticesIncluded(QueryContext) instead
   * TODO: Migrate tests to QueryContext-only constructor
   */
  @Deprecated
  public ResponseValidationNoticesIncluded(String rdapResponse, RDAPValidatorResults results, RDAPQueryType queryType) {
    super(rdapResponse, results);
    this.queryType = queryType;
    this.queryContext = null; // Not available in deprecated constructor
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_notices_included_Validation";
  }

  @Override
  protected boolean doValidate() {
    if (getPointerFromJPath("$..notices").isEmpty()) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
          .code(-46500)
          .value(jsonObject.toString())
          .message("A notices members does not appear in the RDAP response.");

      if (queryContext != null) {
        results.add(builder.build(queryContext));
      } else {
        results.add(builder.build()); // Fallback for deprecated constructor
      }
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
