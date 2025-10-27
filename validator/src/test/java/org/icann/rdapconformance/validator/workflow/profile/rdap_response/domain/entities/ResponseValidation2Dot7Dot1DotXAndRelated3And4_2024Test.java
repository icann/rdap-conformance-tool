package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import java.io.IOException;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

    @Override
    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        replaceValue("['entities'][0]['handle']", "2138514_DOMAIN_COM-EXMP");
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
        return new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(domainContext);
    }

    @Test
    public void testValidate_HandleIsInvalid_AddErrorCode() {
        entitiesWithRole("registrant");
        replaceValue("['entities'][0]['handle']", "2138514_DOMAIN_COM-ICANNRST");
        validate(-52106, "#/entities/0/handle:2138514_DOMAIN_COM-ICANNRST",
            "The globally unique identifier in the entity object handle is using an EPPROID reserved for testing by ICANN.");
    }
}