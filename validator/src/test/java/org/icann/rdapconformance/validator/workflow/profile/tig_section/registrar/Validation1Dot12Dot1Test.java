package org.icann.rdapconformance.validator.workflow.profile.tig_section.registrar;

import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigJsonValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidationFromSchemaTestBase;
import org.testng.annotations.Test;

public class Validation1Dot12Dot1Test extends TigValidationFromSchemaTestBase {

  public Validation1Dot12Dot1Test() {
    super(
        "rdap_entities.json",
        "/validators/profile/tig_section/entities/valid.json",
        "tigSection_1_12_1_Validation");
  }

  @Override
  public TigJsonValidation getTigValidation() {
    return new Validation1Dot12Dot1(jsonObject.toString(), results);
  }

  /**
   * 8.3.1.1
   */
  @Test
  public void testValidate_RegistrarEntityWithoutPublicIdsIdentifier_AddResults26100() {
    removeKey("$['entities'][1]['publicIds'][1]['identifier']");
    validate(-26100, "#/entities/1/publicIds/1:{\"type\":\"IANA Registrar ID\"}",
        "An identifier in the publicIds within the entity data "
            + "structure with the registrar role was not found. See section 1.12.1 of the "
            + "RDAP_Technical_Implementation_Guide_2_1.");
  }
}