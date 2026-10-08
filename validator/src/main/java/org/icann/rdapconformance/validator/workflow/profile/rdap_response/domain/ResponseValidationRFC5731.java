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

  private static final String ACTIVE = "active";
  private static final String PENDING_CREATE = "pending create";
  private static final String PENDING_RENEW = "pending renew";
  private static final String PENDING_UPDATE = "pending update";
  private static final String PENDING_DELETE = "pending delete";
  private static final String PENDING_TRANSFER = "pending transfer";

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
          PENDING_CREATE,
          PENDING_RENEW,
          PENDING_UPDATE,
          PENDING_DELETE,
          PENDING_TRANSFER);

  // At most one of these "pending*" statuses may be present at a time (46900(c))
  private static final Set<String> PENDING_STATUSES = Set.of(
          PENDING_CREATE,
          PENDING_DELETE,
          PENDING_RENEW,
          PENDING_TRANSFER,
          PENDING_UPDATE);

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

    if (hasInvalidStatusCombination(status)) {
      RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
              .code(-46900)
              .value(getResultValue("#/status"))
              .message("The values of the status data structure does not comply with RFC5731.");

      results.add(builder.build(queryContext));
      return false;
    }
    return true;
  }

  private static boolean hasInvalidStatusCombination(Set<String> status) {
    return combinesActiveWithProhibitedStatus(status)
            || combinesPendingWithProhibited(status)
            || hasMultiplePendingStatuses(status)
            || combinesRedemptionPeriodWithPendingRestore(status);
  }

  /** 46900(a): "active" must not be combined with any denylisted status. */
  private static boolean combinesActiveWithProhibitedStatus(Set<String> status) {
    return status.contains(ACTIVE)
            && status.stream().anyMatch(ACTIVE_PROHIBITED_STATUSES::contains);
  }

  /** 46900(b): "pending X" must not be combined with "client/server X prohibited". */
  private static boolean combinesPendingWithProhibited(Set<String> status) {
    return pendingConflicts(status, PENDING_DELETE, "delete")
            || pendingConflicts(status, PENDING_RENEW, "renew")
            || pendingConflicts(status, PENDING_TRANSFER, "transfer")
            || pendingConflicts(status, PENDING_UPDATE, "update");
  }

  private static boolean pendingConflicts(Set<String> status, String pendingStatus, String action) {
    return status.contains(pendingStatus)
            && (status.contains("client " + action + " prohibited")
                || status.contains("server " + action + " prohibited"));
  }

  /** 46900(c): at most one "pending*" status may be present. */
  private static boolean hasMultiplePendingStatuses(Set<String> status) {
    return status.stream().filter(PENDING_STATUSES::contains).count() > CommonUtils.ONE;
  }

  /** 46900(g): "redemption period" and "pending restore" cannot be combined. */
  private static boolean combinesRedemptionPeriodWithPendingRestore(Set<String> status) {
    return status.containsAll(Set.of("redemption period", "pending restore"));
  }

  @Override
  public boolean doLaunch() {
    return queryType.equals(RDAPQueryType.DOMAIN);
  }
}