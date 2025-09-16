package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.testng.annotations.Test;

public class ResponseValidation2Dot10Test extends ResponseDomainValidationTestBase {


  public ResponseValidation2Dot10Test() {
    super("rdapResponseProfile_2_10_Validation");
  }

  @Override
  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot10(jsonObject.toString(), results, config, queryType);
  }

  @Test
  public void testValidate_SecureDNSAbsent_AddResults46800() {
    removeKey("secureDNS");
    validate(-46800, jsonObject.toString(),
        "A secureDNS member does not appear in the domain object.");
  }

  @Test
  public void testValidate_delegationSignedAbsent_AddResults46801() {
    removeKey("secureDNS.delegationSigned");
    validate(-46801, jsonObject.toString(),
        "The delegationSigned element does not exist.");
  }

  @Test
  public void testValidate_dsDataAndKeyDataAbsent_AddResults46802() {
    replaceValue("secureDNS.delegationSigned", true);
    removeKey("secureDNS.dsData");
    validate(-46802, jsonObject.toString(),
        "delegationSigned value is true, but no dsData nor keyData "
            + "name/value pair exists.");
  }
}