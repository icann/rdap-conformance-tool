package org.icann.rdapconformance.validator;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorNameserverTest extends SchemaValidatorObjectTest {

  public SchemaValidatorNameserverTest() {
    super("nameserver",
        "rdap_nameserver.json",
        "/validators/nameserver/valid.json",
        -12400,
        -12401,
        -12402,
        List.of(
            "objectClassName",
            "handle",
            "lang",
            "ldhName",
            "unicodeName",
            "ipAddresses",
            "entities",
            "status",
            "remarks",
            "links",
            "port43",
            "events",
            "notices",
            "rdapConformance"
        )
    );
  }

  @Test
  public void validateObjectClassName() {
    testWrongConstant(-12403, "objectClassName", "nameserver");
  }

  @Test
  public void validateHandle() {
    validateIsNotAJsonString(-12404, "handle");
  }

  @Test
  public void stdRdapLdhNameValidation() {
    stdRdapLdhNameValidation(-12405);
  }

  @Test
  public void stdRdapUnicodeNameValidation() {
    stdRdapUnicodeNameValidation(-12406);
  }

  @Test
  public void stdRdapIpAddressesValidation() {
    validateSubValidation("stdRdapIpAddressesValidation", "ipAddresses", -12407);
  }

  @Test
  public void stdRdapEntitiesValidation() {
    stdRdapEntitiesValidation(-12408);
  }

  @Test
  public void stdRdapStatusValidation() {
    stdRdapStatusValidation(-12409);
  }

  @Test
  public void stdRdapRemarksValidation() {
    stdRdapRemarksValidation(-12410);
  }

  @Test
  public void stdRdapLinksValidation() {
    stdRdapLinksValidation(-12411);
  }

  @Test
  public void stdRdapPort43WhoisServerValidation() {
    stdRdapPort43WhoisServerValidation(-12412);
  }

  @Test
  public void stdRdapEventsValidation() {
    stdRdapEventsValidation(-12413);
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    stdRdapNoticesRemarksValidation(-12414);
  }

  @Test
  public void stdRdapConformanceValidation() {
    stdRdapConformanceValidation(-12416);
  }
}
