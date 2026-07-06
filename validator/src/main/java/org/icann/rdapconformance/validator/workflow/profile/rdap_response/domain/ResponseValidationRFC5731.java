package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.icann.rdapconformance.validator.CommonUtils;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;

public final class ResponseValidationRFC5731 extends ProfileJsonValidation {

  // Statuses that MUST NOT be combined with "active" (denylist per RDAP profile 46900(a))
  private static final Set<String> ACTIVE_PROHIBITED_STATUSES = Set.of(
          "inactive",
          "client hold",
          "client renew prohibited",
          "client delete prohibited",
          "client transfer prohibited",
          "client update prohibited",
          "server hold",
          "server renew prohibited",
          "server delete prohibited",
          "server transfer prohibited",
          "server update prohibited",
          "pending create",
          "pending renew",
          "pending update",
          "pending delete",
          "pending transfer");

  // At most one of these "pending*" statuses may be present at a time (46900(c))
  private static final Set<String> PENDING_STATUSES = Set.of(
          "pending create",
          "pending delete",
          "pending renew",
          "pending transfer",
          "pending update");

  private final RDAPQueryType queryType;
  private final QueryContext queryContext;

  public ResponseValidationRFC5731(QueryContext qctx) {
    super(qctx.getRdapResponseData(), qctx.getResults());
    this.queryType = qctx.getQueryType();
    this.queryContext = qctx;
  }

  @Override
  public String getGroupName() {
    return "rdapResponseProfile_rfc5731_Validation";
  }

  @Override
  protected boolean doValidate() {
    Set<String> status = new HashSet<>();
    JSONArray statusArray = jsonObject.optJSONArray("status");
    if (statusArray != null) {
      statusArray.forEach(s -> status.add((String) s));
    }

    if ((status.contains("active") && status.stream().anyMatch(ACTIVE_PROHIBITED_STATUSES::contains)) ||
            (status.containsAll(Set.of("pending delete", "client delete prohibited")) ||
                    status.containsAll(Set.of("pending delete", "server delete prohibited"))) ||
            (status.containsAll(Set.of("pending renew", "client renew prohibited")) ||
                    status.containsAll(Set.of("pending renew", "server renew prohibited"))) ||
            (status.containsAll(Set.of("pending transfer", "client transfer prohibited")) ||
                    status.containsAll(Set.of("pending transfer", "server transfer prohibited"))) ||
            (status.containsAll(Set.of("pending update", "client update prohibited")) ||
                    status.containsAll(Set.of("pending update", "server update prohibited"))) ||
            (status.stream().filter(PENDING_STATUSES::contains).count() > CommonUtils.ONE ||
            // (g) "redemption period" and "pending restore" cannot be combined with each other
            status.containsAll(Set.of("redemption period", "pending restore")))) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
              .code(-46900)
              .value(getResultValue("#/status"))
              .message("The values of the status data structure does not comply with RFC5731.");

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