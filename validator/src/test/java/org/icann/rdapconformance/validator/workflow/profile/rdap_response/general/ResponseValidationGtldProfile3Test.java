package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidation;
import org.icann.rdapconformance.validator.workflow.profile.RDAPConformanceValidationTest;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3Test extends RDAPConformanceValidationTest {

    public ResponseValidationGtldProfile3Test() {
        super("gtldProfile3Conformance_Validation");
    }

    @Test
    public void testValidate_ok() {
        jsonObject.put("rdapConformance", List.of("rdap_level_0", "gtldProfile3"));
        validate();
    }

    @Override
    public RDAPConformanceValidation getProfileValidation() {
        return new ResponseValidationGtldProfile3(queryContext);
    }
}
