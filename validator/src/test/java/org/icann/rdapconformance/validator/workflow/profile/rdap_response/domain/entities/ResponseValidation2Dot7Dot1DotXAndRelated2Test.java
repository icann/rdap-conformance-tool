package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidation2Dot7Dot1DotXAndRelated2Test extends
    ResponseValidation2Dot7Dot1DotXAndRelatedTest {

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    String validVcardJson = getResource(
        "/validators/profile/rdap_response/domain/entities/vcard/valid.json");
    jsonObject
        .getJSONArray("entities")
        .getJSONObject(0)
        .put("vcardArray", new JSONArray(validVcardJson));
  }

  public ProfileValidation getProfileValidation() {
    return new ResponseValidation2Dot7Dot1DotXAndRelated2(jsonObject.toString(), results,
        queryType, config);
  }

  @Test
  public void withoutFnWithoutRedactedForPrivacyTitle() {
    validateWithoutProperty("fn");
  }

  @Test
  public void withoutAdrWithoutRedactedForPrivacyTitle() {
    validateWithoutProperty("adr");
  }

  @Test
  public void withoutTelWithoutRedactedForPrivacyTitle() {
    validateWithoutProperty("tel");
  }


  @Test
  public void countryNotIncludedInAdrProperty() {
    // remove all elements including country:
    removeKey("$.['entities'][0]['vcardArray'][1][4][3][2:6]");
    assertThat((List<String>) getValue("$.['entities'][0]['vcardArray'][1][4][3]")).hasSize(3);
    validate52101();
  }

  @Test
  public void emptyStreetInAdrProperty() {
    // Set street (index 2) to empty string
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3][2]", "");
    validate52101();
  }

  @Test
  public void emptyCityInAdrProperty() {
    // Set city (index 3) to empty string
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3][3]", "");
    validate52101();
  }

  @Test
  public void emptyStreetAndCityInAdrProperty() {
    // Set both street (index 2) and city (index 3) to empty strings
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3][2]", "");
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3][3]", "");
    validate52101();
  }

  @Test
  public void whitespaceOnlyStreetInAdrProperty() {
    // Set street (index 2) to whitespace-only string
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3][2]", "   ");
    validate52101();
  }

  @Test
  public void whitespaceOnlyCityInAdrProperty() {
    // Set city (index 3) to whitespace-only string
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3][3]", "   ");
    validate52101();
  }

  // Removed: nonStringStreetInAdrProperty and nonStringCityInAdrProperty 
  // Coverage provided by truncatedAdrArrayMissing* tests

  @Test
  public void truncatedAdrArrayMissingStreet() {
    // Truncate address array to only have 2 elements (missing street at index 2)
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3]", List.of("", ""));
    validate52101();
  }

  @Test
  public void truncatedAdrArrayMissingCity() {
    // Truncate address array to only have 3 elements (missing city at index 3)  
    replaceValue("$.['entities'][0]['vcardArray'][1][4][3]", List.of("", "", "123 Main St"));
    validate52101();
  }

  @Test
  public void entityWithRedactedForPrivacyRemark() {
    // Test the other branch - entity WITH "REDACTED FOR PRIVACY" remark should not trigger validation
    entitiesWithRole("registrant");
    remarkMemberIs("title", "REDACTED FOR PRIVACY"); // This creates the remarks structure properly
    
    // Even with missing properties, should not trigger -52101 when redacted
    removeKey("$.['entities'][0]['vcardArray'][1][*][?(@ == 'fn')]");
    removeKey("$.['entities'][0]['vcardArray'][1][*][?(@ == 'email')]");
    removeKey("$.['entities'][0]['handle']");
    
    validate(); // Should pass without -52101 error
  }

  @Test
  public void childOfRegistrarEntity() {
    // Create an entity that is a child of registrar to test isChildOfRegistrar branch
    replaceValue("$.['entities'][0]['roles']", List.of("registrar"));
    
    // Even without required fields, should not trigger -52101 for registrar entities
    removeKey("$.['entities'][0]['vcardArray'][1][*][?(@ == 'fn')]");
    removeKey("$.['entities'][0]['handle']");
    
    entitiesWithRole("registrar");
    validate(); // Should pass without -52101 error since it's a registrar
  }

  @Test
  public void emptyFnValueInVcard() {
    // Set fn value (index 3) to empty string
    replaceValue("$.['entities'][0]['vcardArray'][1][1][3]", "");
    validate52101();
  }

  @Test
  public void whitespaceOnlyFnValueInVcard() {
    // Set fn value (index 3) to whitespace-only string
    replaceValue("$.['entities'][0]['vcardArray'][1][1][3]", "   ");
    validate52101();
  }

  @Test
  public void emptyHandleValue() {
    // Set handle to empty string
    replaceValue("$.['entities'][0]['handle']", "");
    validate52101();
  }

  @Test
  public void whitespaceOnlyHandleValue() {
    // Set handle to whitespace-only string
    replaceValue("$.['entities'][0]['handle']", "   ");
    validate52101();
  }

  @Test
  public void nonStringHandleValue() {
    // Set handle to non-string value
    replaceValue("$.['entities'][0]['handle']", 12345);
    validate52101();
  }

  @Test
  public void malformedVcardPropertyTriggersException() {
    // IMPORTANT: Tests ValidationException catch block for coverage
    // Create a malformed fn property that will trigger ValidationException
    replaceValue("$.['entities'][0]['vcardArray'][1][1]", List.of("fn")); // Missing required elements
    validate52101();
  }

  @Test 
  public void malformedAdrArrayStructure() {
    // IMPORTANT: Tests Exception catch block in adr validation for coverage
    replaceValue("$.['entities'][0]['vcardArray'][1][4]", List.of("adr", new Object())); // Missing array at index 3  
    validate52101();
  }

  private void validateWithoutProperty(String property) {
    removeKey("$.['entities'][0]['vcardArray'][1][*][?(@ == '" + property + "')]");
    validate52101();
  }

  private void validate52101() {
    entitiesWithRole("registrant");
    remarkMemberIs("title", "NOT REDACTED FOR PRIVACY");
    validate(-52101, "#/entities/0:" + jsonObject.query("#/entities/0"),
            "An entity without a remark titled \"REDACTED FOR PRIVACY\" " +
                    "does not have all the necessary information of handle, fn, adr, tel, street and city.");
  }
}