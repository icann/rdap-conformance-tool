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
    jsonObject.put("handle", 0);
    validateIsNotAJsonString(-12404, "#/handle:0");
  }

  @Test
  public void stdRdapLdhNameValidation() {
    validateSubValidation(ComplexValidation.ofLdhName(-12405));
  }

  @Test
  public void stdRdapUnicodeNameValidation() {
    validateSubValidation(ComplexValidation.ofUnicodeName(-12406));
  }

  @Test
  public void stdRdapIpAddressesValidation() {
    validateSubValidation(
        new ComplexValidation("stdRdapIpAddressesValidation", "ipAddresses", -12407));
  }

  @Test
  public void stdRdapEntitiesValidation() {
    validateSubValidation(ComplexValidation.ofEntities(-12408));
  }

  @Test
  public void stdRdapStatusValidation() {
    validateSubValidation(ComplexValidation.ofStatus(-12409));
  }

  @Test
  public void stdRdapRemarksValidation() {
    validateSubValidation(ComplexValidation.ofRemarks(-12410));
  }

  @Test
  public void stdRdapLinksValidation() {
    validateSubValidation(ComplexValidation.ofLinks(-12411));
  }

  @Test
  public void stdRdapPort43WhoisServerValidation() {
    validateSubValidation(ComplexValidation.ofPort43(-12412));
  }

  @Test
  public void stdRdapEventsValidation() {
    validateSubValidation(ComplexValidation.ofEvents(-12413));
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    validateSubValidation(ComplexValidation.ofNotices(-12414));
  }

  @Test
  public void stdRdapConformanceValidation() {
    validateSubValidation(ComplexValidation.ofRdapConformance(-12416));
  }
}
