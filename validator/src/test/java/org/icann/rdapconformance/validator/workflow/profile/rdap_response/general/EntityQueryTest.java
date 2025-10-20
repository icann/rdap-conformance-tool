package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

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

    @Override
    public ProfileValidation getProfileValidation() {
        // This won't be used in our specific tests
        return new ResponseValidation2Dot2_1_2024(
            jsonObject.toString(), results, datasets, RDAPQueryType.DOMAIN);
    }

    @Override
    @Test
    public void testValidate_ok() {
        // Skip the automatic validation test since this test class is specifically
        // testing doLaunch() behavior for different query types, not validation results
    }

    @Test
    public void testEntityQuery_DoLaunchReturnsFalse() {
        // Test that ResponseValidation2Dot2_1_2024 doesn't run on entity queries
        ResponseValidation2Dot2_1_2024 validation = new ResponseValidation2Dot2_1_2024(
            jsonObject.toString(), results, datasets, RDAPQueryType.ENTITY);

        // Check that doLaunch() returns false for entity queries
        assertThat(validation.doLaunch()).isFalse();
    }

    @Test
    public void testDomainQuery_DoLaunchReturnsTrue() {
        // Test that ResponseValidation2Dot2_1_2024 runs on domain queries
        ResponseValidation2Dot2_1_2024 validation = new ResponseValidation2Dot2_1_2024(
            jsonObject.toString(), results, datasets, RDAPQueryType.DOMAIN);

        // Check that doLaunch() returns true for domain queries
        assertThat(validation.doLaunch()).isTrue();
    }

    @Test
    public void testOtherQueryTypes_DoLaunchReturnsFalse() {
        // Test other query types also return false
        ResponseValidation2Dot2_1_2024 validation;

        validation = new ResponseValidation2Dot2_1_2024(
            jsonObject.toString(), results, datasets, RDAPQueryType.NAMESERVER);
        assertThat(validation.doLaunch()).isFalse();

        validation = new ResponseValidation2Dot2_1_2024(
            jsonObject.toString(), results, datasets, RDAPQueryType.AUTNUM);
        assertThat(validation.doLaunch()).isFalse();

        validation = new ResponseValidation2Dot2_1_2024(
            jsonObject.toString(), results, datasets, RDAPQueryType.IP_NETWORK);
        assertThat(validation.doLaunch()).isFalse();
    }
}