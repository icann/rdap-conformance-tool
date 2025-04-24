package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.testng.annotations.Test;

public class StdRdapNameserversSearchValidation_2024Test  extends ProfileJsonValidationTestBase {


    public StdRdapNameserversSearchValidation_2024Test() {
        super("/validators/domain/valid.json", "stdRdapNameserversSearchValidation");
    }

    @Override
    public ProfileJsonValidation getProfileValidation() {
        return new StdRdapNameserversSearchValidation_2024(jsonObject.toString(), results);
    }

    @Override
    public void testValidate_ok() {
        jsonObject.put("nameserverSearchResults", "dummy");
        validate();
    }

    @Test
    public void testValidate_ContainsRdapConformance_AddResults12610() {
        validate(-12610, jsonObject.toString(),
            "The nameserverSearchResults structure is required");
    }
}
