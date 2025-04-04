package org.icann.rdapconformance.validator.schemavalidator;

import org.testng.annotations.Test;

import java.util.List;

public class SchemaValidatorIpNetworkTest extends SchemaValidatorObjectTest {

  public SchemaValidatorIpNetworkTest() {
    super("ipNetwork",
        "rdap_ip_network.json",
        "/validators/ipNetwork/valid.json",
        -12300,
        -12301,
        -12302,
        List.of(
  "objectClassName",
            "handle",
            "startAddress",
            "endAddress",
            "ipVersion",
            "name",
            "type",
            "country",
            "parentHandle",
            "entities",
            "remarks",
            "links",
            "events",
            "status",
            "port43",
            "rdapConformance",
            "notices"
        )
    );
    validationName = "stdRdapIpNetworkLookupValidation";
  }

  @Test
  public void validateObjectClassName() {
    testWrongConstant(-12303, "objectClassName", "ip network");
  }

  @Test
  public void validateHandle() {
    validateIsNotAJsonString(-12304, "handle");
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

}
