package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

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

    @Override
    public void testDoLaunch() {
        // Test doLaunch behavior by creating validation instances with different query types
        // Domain validations should return true only when query type is DOMAIN

        QueryContext helpContext = new QueryContext(queryContext.getQueryId(),
            queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.HELP);
        helpContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(helpContext).doLaunch()).isFalse();

        QueryContext nameserversContext = new QueryContext(queryContext.getQueryId(),
            queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVERS);
        nameserversContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(nameserversContext).doLaunch()).isFalse();

        QueryContext nameserverContext = new QueryContext(queryContext.getQueryId(),
            queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVER);
        nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(nameserverContext).doLaunch()).isFalse();

        QueryContext entityContext = new QueryContext(queryContext.getQueryId(),
            queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.ENTITY);
        entityContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(entityContext).doLaunch()).isFalse();

        QueryContext domainContext = new QueryContext(queryContext.getQueryId(),
            queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.DOMAIN);
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        assertThat(new ResponseValidation2Dot7Dot1DotXAndRelated3And4_2024(domainContext).doLaunch()).isTrue();
    }


    @Test
    public void testValidate_HandleIsInvalid_AddErrorCode() {
        entitiesWithRole("registrant");
        replaceValue("['entities'][0]['handle']", "2138514_DOMAIN_COM-ICANNRST");
        validate(-52106, "#/entities/0/handle:2138514_DOMAIN_COM-ICANNRST",
            "The globally unique identifier in the entity object handle is using an EPPROID reserved for testing by ICANN.");
    }
}