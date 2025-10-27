package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.List;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.testng.annotations.Test;

public class StdRdapConformanceValidation_2024Test  extends ProfileJsonValidationTestBase {


    public StdRdapConformanceValidation_2024Test() {
        super("/validators/domain/valid.json", "stdRdapConformanceValidation");
    }

    @Override
    public ProfileJsonValidation getProfileValidation() {
        return new StdRdapConformanceValidation_2024(queryContext);
    }

    @Test
    public void testValidate_ContainsRdapConformance_AddResults10612() {
        removeKey("rdapConformance");
        validate(-10504, jsonObject.toString(),
            "RFC 9083 requires all RDAP responses to have an rdapConformance array.");
    }

    @Test
    public void testValidate_ContainsRdapConformanceInChild_AddResults10613() {
        putValue("$.secureDNS", "rdapConformance", List.of("dummy"));
        validate(-10505, "#/secureDNS/rdapConformance",
            "The rdapConformance array must appear only in the top-most of the RDAP response.");
    }
}
