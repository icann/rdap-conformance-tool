package org.icann.rdapconformance.validator.schemavalidator;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorDomainTest extends SchemaValidatorObjectTest {

  public SchemaValidatorDomainTest() {
    super(
        "domain",
        "rdap_domain.json",
        "/validators/domain/valid.json",
        -12200,
        -12201,
        -12202,
        List.of("objectClassName", "handle",
            "ldhName", "unicodeName", "variants", "nameservers", "secureDNS", "entities", "status",
            "publicIds", "remarks", "links", "port43", "events", "notices", "rdapConformance",
            "lang")
    );
  }

  @Test
  public void validateObjectClassName() {
    testWrongConstant(-12203, "objectClassName", "domain");
  }

  @Test
  public void validateHandle() {
    validateIsNotAJsonString(-12204, "handle");
  }

  @Test
  public void stdRdapLdhNameValidation() {
    stdRdapLdhNameValidation(-12205);
  }

  @Test
  public void stdRdapUnicodeNameValidation() {
    stdRdapUnicodeNameValidation(-12206);
  }

  @Test
  public void stdRdapVariantsValidation() {
    validateSubValidation("stdRdapVariantsValidation", "variants", -12207);
  }

  @Test
  public void stdRdapNameserverLookupValidation() {
    validateSubValidation("stdRdapNameserverLookupValidation", "nameservers", -12208);
  }

  @Test
  public void stdRdapSecureDnsValidation() {
    validateSubValidation("stdRdapSecureDnsValidation", "secureDNS", -12209);
  }

  @Test
  public void stdRdapEntitiesValidation() {
    stdRdapEntitiesValidation(-12210);
  }

  @Test
  public void stdRdapStatusValidation() {
    stdRdapStatusValidation(-12211);
  }

  @Test
  public void stdRdapPublicIdsValidation() {
    stdRdapPublicIdsValidation(-12212);
  }

  @Test
  public void stdRdapRemarksValidation() {
    stdRdapRemarksValidation(-12213);
  }

  @Test
  public void stdRdapLinksValidation() {
    stdRdapLinksValidation(-12214);
  }

  @Test
  public void stdRdapPort43WhoisServerValidation() {
    stdRdapPort43WhoisServerValidation(-12215);
  }

  @Test
  public void stdRdapEventsValidation() {
    stdRdapEventsValidation(-12216);
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    stdRdapNoticesRemarksValidation(-12217);
  }

  @Test
  public void stdRdapConformanceValidation() {
    stdRdapConformanceValidation(-12219);
  }

  @Test
  public void testInvalidVariantsSubProperty() {
    JSONObject variant = new JSONObject();
    variant.put("relation", 0);
    JSONArray jsonArray = new JSONArray(List.of(variant));
    jsonObject.put("variants", jsonArray);
    validateSubValidation("stdRdapVariantsValidation", "#/variants/0/relation:0", -12207);
  }
}