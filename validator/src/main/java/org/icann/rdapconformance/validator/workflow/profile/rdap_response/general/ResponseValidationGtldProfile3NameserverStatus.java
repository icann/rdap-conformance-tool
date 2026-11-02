package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;

/**
 * gTLD Profile v3 conformance: when the query returns a nameserver object, the object
 * must have a "status" member. Emits -66008 with the nameserver object otherwise.
 *
 * <p>Scope: launched only for nameserver lookups ({@link RDAPQueryType#NAMESERVER}). Nameserver
 * search results ({@code /nameservers?ip=} / {@code ?name=}) are deliberately out of scope: the
 * test case text covers the case where "the query returns a nameserver object", and unlike
 * -66005 for entities, the profile defines no nameserver search-result test case.
 *
 * <p>Only the presence of the member is checked; {@code "status": []} passes. Status values
 * themselves are validated by -49300 (RFC 5732) and the nameserver schema.
 */
public final class ResponseValidationGtldProfile3NameserverStatus extends ProfileJsonValidation {

    private static final String STATUS = "status";

    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3NameserverStatus(QueryContext queryContext) {
        super(queryContext.getRdapResponseData(), queryContext.getResults());
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3NameserverStatus_Validation";
    }

    @Override
    public boolean doLaunch() {
        return RDAPQueryType.NAMESERVER.equals(queryContext.getQueryType());
    }

    @Override
    protected boolean doValidate() {
        if (jsonObject.has(STATUS)) {
            return true;
        }
        results.add(RDAPValidationResult.builder()
            .code(-66008)
            .value(jsonObject.toString())
            .message("The nameserver object must have a 'status' member.")
            .build(queryContext));
        return false;
    }
}
