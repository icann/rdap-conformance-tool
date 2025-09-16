package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;


public class ResponseValidation2Dot2_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidation2Dot2_2024Test() {
        super("/validators/domain/valid.json", "rdapResponseProfile_2_1_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot2_2024(jsonObject.toString(), results, config, RDAPQueryType.DOMAIN);
    }

    protected String givenReservedICANNHandle() {
        replaceValue("handle", "12345678-ICANNRST");
        return "#/handle:12345678-ICANNRST";
    }

    @Test
    public void testValidate_HandleIsInvalid_AddErrorCode() {
            String value = givenReservedICANNHandle();
            getProfileValidation();
            validate(-46205, value,
                "The globally unique identifier in the domain object handle is using an EPPROID reserved for testing by ICANN.");
    }
}