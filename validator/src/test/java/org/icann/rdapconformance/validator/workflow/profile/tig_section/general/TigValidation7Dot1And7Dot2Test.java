package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.testng.annotations.Test;

public class TigValidation7Dot1And7Dot2Test extends ProfileJsonValidationTestBase {

  public TigValidation7Dot1And7Dot2Test() {
    super(
        "/validators/domain/valid.json",
        "tigSection_7_1_and_7_2_Validation");
  }

  public ProfileJsonValidation getProfileValidation() {
    return new TigValidation7Dot1And7Dot2(jsonObject.toString(), results, queryContext);
  }

  /**
   * 8.1.9
   */
  @Test
  public void tigSection_4_1_Validation() {
    // replace the type == voice/fax valid with a wrong value:
    Map<String, String> wrongType = Map.of("type", "not-voice-nor-fax");
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", wrongType);
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":\"not-voice-nor-fax\"}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }

  @Test
  public void voiceType() {
    // replace the type == voice/fax valid with an alternate valid syntax
    // ({"type": "voice"} OR --> {"type": ["voice"]} <-- are both valid)
    Map<String, List<String>> validAlternativeType = Map.of("type", List.of("voice"));
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", validAlternativeType);
    validate();
  }

  @Test
  public void testTypeArrayWithVoiceFirst_ShouldPass() {
    // Test case: Array with voice first - should pass (current logic works)
    Map<String, List<String>> validTypeArray = Map.of("type", List.of("voice", "work"));
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", validTypeArray);
    validate(); // Should pass without errors
  }

  @Test
  public void testTypeArrayWithFaxFirst_ShouldPass() {
    // Test case: Array with fax first - should pass (current logic works)
    Map<String, List<String>> validTypeArray = Map.of("type", List.of("fax", "work"));
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", validTypeArray);
    validate(); // Should pass without errors
  }

  @Test 
  public void testTypeArrayWithVoiceSecond_NowPassesAfterFix() {
    // Test case: Array with voice second - should now pass after fix
    // This SHOULD pass (voice is present) and NOW DOES pass (checks all elements)
    Map<String, List<String>> validTypeArray = Map.of("type", List.of("work", "voice"));
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", validTypeArray);
    
    // After the fix: validation passes because all elements are checked and "voice" is found
    validate(); // Should pass without errors
  }

  @Test
  public void testTypeArrayWithFaxSecond_NowPassesAfterFix() {
    // Test case: Array with fax second - should now pass after fix
    // This SHOULD pass (fax is present) and NOW DOES pass (checks all elements)
    Map<String, List<String>> validTypeArray = Map.of("type", List.of("home", "fax"));
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", validTypeArray);
    
    // After the fix: validation passes because all elements are checked and "fax" is found
    validate(); // Should pass without errors
  }

  @Test
  public void testTypeArrayWithMultipleValidTypes_NowPassesAfterFix() {
    // Test case: Array with multiple valid types but voice not first
    // This must pass (both voice and fax are present)
    Map<String, List<String>> multipleValidTypes = Map.of("type", List.of("cell", "voice", "fax", "work"));
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", multipleValidTypes);
    
    // After the fix: validation passes because all elements are checked and both "voice" and "fax" are found
    validate(); // Should pass without errors
  }

  @Test
  public void testTypeArrayWithNoValidTypes_ShouldFail() {
    // Test case: Array with no valid types - should correctly fail
    Map<String, List<String>> invalidTypeArray = Map.of("type", List.of("home", "cell", "work"));
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", invalidTypeArray);
    
    // This should fail  because no voice or fax is present
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":[\"home\",\"cell\",\"work\"]}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }

  @Test
  public void testSingleStringType_ShouldPass() {
    // Test case: Single string type - should pass (current logic works)
    Map<String, String> validSingleType = Map.of("type", "voice");
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", validSingleType);
    validate(); // Should pass without errors
  }

  // Bad data handling tests
  @Test
  public void testTypeArrayWithNullElements_ShouldNotCrash() {
    // Test case: Array containing null elements - should skip nulls and find valid types
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", Arrays.asList(null, "voice", null)));
    validate(); // Should pass - found "voice" despite nulls
  }

  @Test
  public void testTypeArrayWithOnlyNullElements_ShouldFail() {
    // Test case: Array containing only null elements - should fail validation
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", Arrays.asList(null, null)));
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":[null,null]}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }

  @Test
  public void testTypeArrayWithNumberElements_ShouldNotCrash() {
    // Test case: Array containing numbers - should skip numbers and find valid types
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", Arrays.asList(123, "fax", 456)));
    validate(); // Should pass - found "fax" despite numbers
  }

  @Test
  public void testTypeArrayWithBooleanElements_ShouldNotCrash() {
    // Test case: Array containing booleans - should skip booleans and find valid types
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", Arrays.asList(true, false, "voice")));
    validate(); // Should pass - found "voice" despite booleans
  }

  @Test
  public void testTypeArrayWithObjectElements_ShouldNotCrash() {
    // Test case: Array containing objects - should skip objects and continue
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", Arrays.asList(Map.of("bad", "data"), "voice")));
    validate(); // Should pass - found "voice" despite object
  }

  @Test
  public void testTypeArrayWithMixedInvalidElements_ShouldFail() {
    // Test case: Array with mixed invalid elements and no valid ones - should fail
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", Arrays.asList(123, true, null, Map.of("x", "y"))));
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":[123,true,null,{\"x\":\"y\"}]}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }

  @Test
  public void testEmptyTypeArray_ShouldFail() {
    // Test case: Empty array - should fail validation
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", Arrays.asList()));
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":[]}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }

  @Test
  public void testTypeAsNumber_ShouldFail() {
    // Test case: Type as number instead of string or array - should fail
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", 123));
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":123}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }

  @Test
  public void testCaseSensitivity_VoiceUppercase_ShouldFail() {
    // Test case: Case sensitivity - "VOICE" should not match "voice"
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", "VOICE"));
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":\"VOICE\"}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }

  @Test
  public void testWhitespaceHandling_ShouldFail() {
    // Test case: Whitespace handling - " voice " should not match "voice"
    replaceValue("$['entities'][0]['entities'][0]['vcardArray'][1][2][1]", 
        Map.of("type", " voice "));
    validate(-20900,
        "#/entities/0/entities/0/vcardArray/1/2:{\"type\":\" voice \"}",
        "An entity with a tel property without a voice or fax "
            + "type was found. See section 7.1 and 7.2 of the TIG.");
  }
}