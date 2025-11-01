package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class EntityQueryTest extends ProfileJsonValidationTestBase {

    public EntityQueryTest() {
        super("/validators/profile/response_validations/general/entity_query_test.json",
                "rdapResponseProfile_EntityQuery_Test");
    }

    public ProfileValidation getProfileValidation() {
        // This won't be used in our specific tests
        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        return new ResponseValidation2Dot2_1_2024(domainContext);
    }

    @Test
    public void testValidate_ok() {
        // Skip the automatic validation test since this test class is specifically
        // testing doLaunch() behavior for different query types, not validation results
    }

    @Test
    public void testEntityQuery_DoLaunchReturnsFalse() {
        // Test that ResponseValidation2Dot2_1_2024 doesn't run on entity queries
        QueryContext entityContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.ENTITY
        );
        entityContext.setRdapResponseData(queryContext.getRdapResponseData());
        ResponseValidation2Dot2_1_2024 validation = new ResponseValidation2Dot2_1_2024(entityContext);

        // Check that doLaunch() returns false for entity queries
        assertThat(validation.doLaunch()).isFalse();
    }

    @Test
    public void testDomainQuery_DoLaunchReturnsTrue() {
        // Test that ResponseValidation2Dot2_1_2024 runs on domain queries
        QueryContext domainContext = new QueryContext(
            queryContext.getQueryId(),
            queryContext.getConfig(),
            queryContext.getDatasetService(),
            queryContext.getQuery(),
            queryContext.getResults(),
            RDAPQueryType.DOMAIN
        );
        domainContext.setRdapResponseData(queryContext.getRdapResponseData());
        ResponseValidation2Dot2_1_2024 validation = new ResponseValidation2Dot2_1_2024(domainContext);

        // Check that doLaunch() returns true for domain queries
        assertThat(validation.doLaunch()).isTrue();
    }

    @Test
    public void testOtherQueryTypes_DoLaunchReturnsFalse() {
        // Test other query types also return false
        ResponseValidation2Dot2_1_2024 validation;

        QueryContext nameserverContext = new QueryContext(
            queryContext.getQueryId(), queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.NAMESERVER);
        nameserverContext.setRdapResponseData(queryContext.getRdapResponseData());
        validation = new ResponseValidation2Dot2_1_2024(nameserverContext);
        assertThat(validation.doLaunch()).isFalse();

        QueryContext autnumContext = new QueryContext(
            queryContext.getQueryId(), queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.AUTNUM);
        autnumContext.setRdapResponseData(queryContext.getRdapResponseData());
        validation = new ResponseValidation2Dot2_1_2024(autnumContext);
        assertThat(validation.doLaunch()).isFalse();

        QueryContext ipNetworkContext = new QueryContext(
            queryContext.getQueryId(), queryContext.getConfig(), queryContext.getDatasetService(),
            queryContext.getQuery(), queryContext.getResults(), RDAPQueryType.IP_NETWORK);
        ipNetworkContext.setRdapResponseData(queryContext.getRdapResponseData());
        validation = new ResponseValidation2Dot2_1_2024(ipNetworkContext);
        assertThat(validation.doLaunch()).isFalse();
    }
}