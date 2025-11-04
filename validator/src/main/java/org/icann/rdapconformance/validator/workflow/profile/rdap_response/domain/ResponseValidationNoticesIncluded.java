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

      results.add(builder.build(queryContext));
      return false;
    }
    return true;
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}
