package org.icann.rdapconformance.validator;

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
    jsonObject.put("handle", 0);
    validateIsNotAJsonString(-12204, "#/handle:0");
  }

  @Test
  public void stdRdapLdhNameValidation() {
    validateSubValidation(
        ComplexValidation.ofLdhName(-12205));
  }

  @Test
  public void stdRdapUnicodeNameValidation() {
    validateSubValidation(
        ComplexValidation.ofUnicodeName(-12206));
  }

  @Test
  public void stdRdapVariantsValidation() {
    validateSubValidation(
        new ComplexValidation("stdRdapVariantsValidation", "variants", -12207));
  }

  @Test
  public void stdRdapNameserverLookupValidation() {
    validateSubValidation(
        new ComplexValidation("stdRdapNameserverLookupValidation", "nameservers", -12208));
  }

  @Test
  public void stdRdapSecureDnsValidation() {
    validateSubValidation(new ComplexValidation("stdRdapSecureDnsValidation", "secureDNS", -12209));
  }

  @Test
  public void stdRdapEntitiesValidation() {
    validateSubValidation(ComplexValidation.ofEntities(-12210));
  }

  @Test
  public void stdRdapStatusValidation() {
    validateSubValidation(ComplexValidation.ofStatus(-12211));
  }

  @Test
  public void stdRdapPublicIdsValidation() {
    validateSubValidation(ComplexValidation.ofPublicIds(-12212));
  }

  @Test
  public void stdRdapRemarksValidation() {
    validateSubValidation(ComplexValidation.ofRemarks(-12213));
  }

  @Test
  public void stdRdapLinksValidation() {
    validateSubValidation(ComplexValidation.ofLinks(-12214));
  }

  @Test
  public void stdRdapPort43WhoisServerValidation() {
    validateSubValidation(ComplexValidation.ofPort43(-12215));
  }

  @Test
  public void stdRdapEventsValidation() {
    validateSubValidation(ComplexValidation.ofEvents(-12216));
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    validateSubValidation(ComplexValidation.ofNotices(-12217));
  }

  @Test
  public void stdRdapConformanceValidation() {
    validateSubValidation(ComplexValidation.ofRdapConformance(-12219));
  }

  @Test
  public void testInvalidVariantsSubProperty() {
    JSONObject variant = new JSONObject();
    variant.put("relation", 0);
    JSONArray jsonArray = new JSONArray(List.of(variant));
    jsonObject.put("variants", jsonArray);
    validateSubValidation(-12207, "stdRdapVariantsValidation", "#/variants/0/relation:0");
  }
}