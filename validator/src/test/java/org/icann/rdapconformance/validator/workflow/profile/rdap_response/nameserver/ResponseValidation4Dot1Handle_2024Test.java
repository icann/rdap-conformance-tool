package org.icann.rdapconformance.validator.workflow.profile.rdap_response.nameserver;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.testng.annotations.Test;

public class ResponseValidation4Dot1Handle_2024Test extends ProfileJsonValidationTestBase {

    public ResponseValidation4Dot1Handle_2024Test() {
        super("/validators/nameserver/valid.json", "rdapResponseProfile_4_1_Validation");
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation4Dot1Handle_2024(jsonObject.toString(), results, config, RDAPQueryType.NAMESERVER);
    }

    protected String givenReservedICANNHandle() {
        replaceValue("handle", "ABCD-ICANNRST");
        return "#/handle:ABCD-ICANNRST";
    }


    @Test
    public void testValidate_HandleIsInvalid_AddErrorCode() {
        String value = givenReservedICANNHandle();
        getProfileValidation();
        validate(-49104, value,
            "The globally unique identifier in the nameserver object handle is using an EPPROID reserved for testing by ICANN.");
    }
}