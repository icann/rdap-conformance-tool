package org.icann.rdapconformance.validator.schemavalidator;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
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
    validationName = "stdRdapEntityLookupValidation";
  }

  @Test
  public void validateObjectClassName() {
    testWrongConstant(-12303, "objectClassName", "entity");
  }

  @Test
  public void validateHandle() {
    validateIsNotAJsonString(-12304, "handle");
  }

  @Test
  public void validateVcardArray() {
    jsonObject.put("vcardArray", 0);
    validateInvalidJson(-12305, "#/vcardArray:0");
  }

  @Test
  public void stdRdapRolesValidation() {
    stdRdapRolesValidation(-12306);
  }

  @Test
  public void stdRdapAsEventActorValidation() {
    stdRdapAsEventActorValidation(-12312);
  }

  @Test
  public void stdRdapEntitiesValidation() {
    stdRdapEntitiesValidation(-12308);
  }

  @Test
  public void stdRdapStatusValidation() {
    stdRdapStatusValidation(-12313);
  }

  @Test
  public void stdRdapPublicIdsValidation() {
    stdRdapPublicIdsValidation(-12307);
  }

  @Test
  public void stdRdapRemarksValidation() {
    stdRdapRemarksValidation(-12309);
  }

  @Test
  public void stdRdapLinksValidation() {
    stdRdapLinksValidation(-12310);
  }

  @Test
  public void stdRdapPort43WhoisServerValidation() {
    stdRdapPort43WhoisServerValidation(-12314);
  }

  @Test
  public void stdRdapEventsValidation() {
    stdRdapEventsValidation(-12311);
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    stdRdapNoticesRemarksValidation(-12315);
  }

  @Test
  public void stdRdapConformanceValidation() {
    stdRdapConformanceValidation(-12317);
  }

  @Test
  public void noticesNotInTopMost() {
    noticesNotInTopMost(-12316);
  }
}
