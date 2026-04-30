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
    public void testValidate_AdrArrayTooShort_AddResults62100() {
        // Remove country element (index 6) from the adr array to simulate a short adr array
        // This guards against IndexOutOfBoundsException being caught as -12305 instead of -62100
        jsonObject = new JSONObject(JsonPath
                .parse(jsonObject.toString())
                .delete("$['entities'][0]['entities'][0]['vcardArray'][1][4][3][6]")
                .jsonString());
        validate(-62100,
                "#/entities/0/entities/0/vcardArray/1/4:"
                        + "[\"adr\",{\"type\":\"work\"},\"text\","
                        + "[\"\",\"Suite 1234\",\"4321 Rue Somewhere\",\"Quebec\",\"QC\",\"G1V 2M2\"]]",
                "All country names MUST be an empty string.");
    }

    @Test
    public void testValidate_EmptyCountry_NoError() {
        // The base fixture already has empty country ("") — should pass with no errors
        validate();
    }
}