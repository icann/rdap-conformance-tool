package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;


public class ResponseValidation2Dot9Dot1And2Dot9Dot2_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidation2Dot9Dot1And2Dot9Dot2_2024Test() {
        super("/validators/domain/valid.json", "rdapResponseProfile_2_9_1_and_2_9_2_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        return new ResponseValidation2Dot9Dot1And2Dot9Dot2_2024(domainContext);
    }

    @Test
    public void testDoValidationFor292_IcannRstHandle_ReturnsFalse() throws Exception {
        replaceValue("$['nameservers'][0]['handle']", "2138514_DOMAIN_COM-ICANNRST");

        validate(-47205, "#/nameservers/0/handle:2138514_DOMAIN_COM-ICANNRST",
            "The globally unique identifier in the domain object handle is using an EPPROID reserved for testing by ICANN.");
    }
}
