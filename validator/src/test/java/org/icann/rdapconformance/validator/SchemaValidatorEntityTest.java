package org.icann.rdapconformance.validator;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorEntityTest extends SchemaValidatorObjectTest {

  public SchemaValidatorEntityTest() {
    super("entity",
        "rdap_entity.json",
        "/validators/entity/valid.json",
        -12300,
        -12301,
        -12302,
        List.of(
            "objectClassName",
            "handle",
            "vcardArray",
            "roles",
            "publicIds",
            "entities",
            "remarks",
            "lang",
            "links",
            "events",
            "asEventActor",
            "status",
            "port43",
            "notices",
            "rdapConformance"
        )
    );
  }

  @Test
  public void validateObjectClassName() {
    testWrongConstant(-12303, "objectClassName", "entity");
  }

  @Test
  public void validateHandle() {
    jsonObject.put("handle", 0);
    validateIsNotAJsonString(-12304, "#/handle:0");
  }

  @Test
  public void validateVcardArray() {
    jsonObject.put("vcardArray", 0);
    validateInvalidJson(-12305, "#/vcardArray:0");
  }

  @Test
  public void stdRdapRolesValidation() {
    validateSubValidation(
        ComplexValidation.ofRoles(-12306));
  }

  @Test
  public void stdRdapAsEventActorValidation() {
    validateSubValidation(
        ComplexValidation.ofAsEventActor(-12312));
  }

  @Test
  public void stdRdapEntitiesValidation() {
    validateSubValidation(ComplexValidation.ofEntities(-12308));
  }

  @Test
  public void stdRdapStatusValidation() {
    validateSubValidation(ComplexValidation.ofStatus(-12313));
  }

  @Test
  public void stdRdapPublicIdsValidation() {
    validateSubValidation(ComplexValidation.ofPublicIds(-12307));
  }

  @Test
  public void stdRdapRemarksValidation() {
    validateSubValidation(ComplexValidation.ofRemarks(-12309));
  }

  @Test
  public void stdRdapLinksValidation() {
    validateSubValidation(ComplexValidation.ofLinks(-12310));
  }

  @Test
  public void stdRdapPort43WhoisServerValidation() {
    validateSubValidation(ComplexValidation.ofPort43(-12314));
  }

  @Test
  public void stdRdapEventsValidation() {
    validateSubValidation(ComplexValidation.ofEvents(-12311));
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    validateSubValidation(ComplexValidation.ofNotices(-12315));
  }

  @Test
  public void stdRdapConformanceValidation() {
    validateSubValidation(ComplexValidation.ofRdapConformance(-12317));
  }

}
