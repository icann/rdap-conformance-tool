package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import java.util.List;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorNameserversSearchTest extends SchemaValidatorObjectTest {

  public SchemaValidatorNameserversSearchTest() {
    super("nameservers",
        "rdap_nameservers.json",
        "/validators/nameservers/valid.json",
        -12600,
        -12601,
        -12602,
        List.of(
            "nameserverSearchResults",
            "lang",
            "remarks",
            "events",
            "notices",
            "rdapConformance"
        )
    );
    validationName = "stdRdapNameserversSearchValidation";
  }

  @Test
  public void stdRdapNameserverLookupValidation() {
    validateSubValidation("stdRdapNameserverLookupValidation", "nameserverSearchResults", -12604);
  }

  @Test
  public void stdRdapRemarksValidation() {
    stdRdapRemarksValidation(-12605);
  }

  @Test
  public void stdRdapEventsValidation() {
    stdRdapEventsValidation(-12606);
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    stdRdapNoticesRemarksValidation(-12607);
  }

  @Test
  public void stdRdapConformanceValidation() {
    stdRdapConformanceValidation(-12609);
  }

  @Test
  public void noticesNotInTopMost() throws IOException {
    // insert an entity under nameserverSearchResults with a notices in it:
    jsonObject
        .getJSONArray("nameserverSearchResults")
        .getJSONObject(0)
        .put("entities", List.of(new JSONObject(getResource("/validators/entity/valid.json"))))
        .getJSONArray("entities")
        .getJSONObject(0).put("notices", jsonObject.get("notices"));

    validate(-12608,
        "#/nameserverSearchResults/0/entities/0/notices:" + jsonObject.query(
            "#/nameserverSearchResults/0/entities/0/notices"),
        "The value for the JSON name notices exists but " + name + " object is "
            + "not the topmost JSON object.");
  }
}
