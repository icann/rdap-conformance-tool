package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

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

  @Override
  public ProfileJsonValidation getProfileValidation() {
    return new TigValidation7Dot1And7Dot2(jsonObject.toString(), results);
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
}