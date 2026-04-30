package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import com.jayway.jsonpath.JsonPath;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class ResponseValidation2024_1_4Test extends ProfileJsonValidationTestBase {

    public ResponseValidation2024_1_4Test() {
        super("/validators/domain/valid.json",
                "rdapResponseProfile2024_1_4_Validation");
    }

    public ProfileJsonValidation getProfileValidation() {
        return new ResponseValidation2024_1_4(queryContext);
    }

    @Test
    public void testValidate_NonEmptyCountry_AddResults62100() {
        jsonObject = new JSONObject(JsonPath
                .parse(jsonObject.toString())
                .set("$['entities'][0]['entities'][0]['vcardArray'][1][4][3][6]", "Canada")
                .jsonString());
        validate(-62100,
                "#/entities/0/entities/0/vcardArray/1/4:"
                        + "[\"adr\",{\"type\":\"work\"},\"text\","
                        + "[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\",\"Canada\"]]",
                "All country names MUST be an empty string.");
    }

    @Test
    public void testValidate_EmptyCountry_NoError() {
        // The base fixture already has empty country ("") — should pass with no errors
        validate();
    }
}