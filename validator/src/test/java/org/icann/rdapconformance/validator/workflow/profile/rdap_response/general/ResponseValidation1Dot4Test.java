package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import com.jayway.jsonpath.JsonPath;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation1Dot4Test extends ProfileJsonValidationTestBase {

  public ResponseValidation1Dot4Test() {
    super("/validators/domain/valid.json",
        "rdapResponseProfile_1_4_Validation");
  }

  public ProfileJsonValidation getProfileValidation() {
    return new ResponseValidation1Dot4(queryContext);
  }

  @Test
  public void testValidate_CountryInVcardAddress_AddResults40400() {
    jsonObject = new JSONObject(JsonPath
        .parse(jsonObject.toString())
        .set("$['entities'][0]['entities'][0]['vcardArray'][1][4][3][6]", "Canada")
        .jsonString());
    validate(-40400,
        "#/entities/0/entities/0/vcardArray/1/4:"
            + "[\"adr\",{\"type\":\"work\"},\"text\","
            + "[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]",
        "A vcard object with a country name parameter with data was found.");
  }
}

