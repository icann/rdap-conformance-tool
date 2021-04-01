package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorDomainTest extends SchemaValidatorTest {

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
            "lang"),
        List.of(
            new SubValidationInfo("stdRdapLdhNameValidation", "ldhName", -12205),
            new SubValidationInfo("stdRdapUnicodeNameValidation", "unicodeName", -12206),
            new SubValidationInfo("stdRdapVariantsValidation", "variants", -12207),
            new SubValidationInfo("stdRdapNameserverLookupValidation", "nameservers", -12208),
            new SubValidationInfo("stdRdapSecureDnsValidation", "secureDNS", -12209),
            new SubValidationInfo("stdRdapEntitiesValidation", "entities", -12210),
            new SubValidationInfo("stdRdapStatusValidation", "status", -12211),
            new SubValidationInfo("stdRdapPublicIdsValidation", "publicIds", -12212),
            new SubValidationInfo("stdRdapNoticesRemarksValidation", "remarks", -12213),
            new SubValidationInfo("stdRdapLinksValidation", "links", -12214),
            new SubValidationInfo("stdRdapPort43WhoisServerValidation", "port43", -12215),
            new SubValidationInfo("stdRdapEventsValidation", "events", -12216),
            new SubValidationInfo("stdRdapNoticesRemarksValidation", "notices", -12213),
            new SubValidationInfo("stdRdapConformanceValidation", "rdapConformance", -12219)
        ));
  }

  @Test
  public void testWrongObjectClassName() {
    jsonObject.put("objectClassName", "not-domain");
    schemaValidator.validate(jsonObject.toString());
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", -12203)
        .hasFieldOrPropertyWithValue("value", "#/objectClassName:not-domain")
        .hasFieldOrPropertyWithValue("message",
            "The JSON value is not domain.");
  }

  @Test
  public void testHandleNotJsonString() {
    jsonObject.put("handle", 0);
    validateIsNotAJsonString(jsonObject.toString(), -12204, "#/handle:0");
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