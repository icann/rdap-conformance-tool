package org.icann.rdapconformance.validator;

import java.util.List;
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
  }

  @Test
  public void stdRdapNameserverLookupValidation() {
    validateSubValidation(new ComplexValidation("stdRdapNameserverLookupValidation", "nameserverSearchResults",
        -12604));
  }

  @Test
  public void stdRdapRemarksValidation() {
    validateSubValidation(ComplexValidation.ofRemarks(-12605));
  }

  @Test
  public void stdRdapEventsValidation() {
    validateSubValidation(ComplexValidation.ofEvents(-12606));
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    validateSubValidation(ComplexValidation.ofNotices(-12607));
  }

  @Test
  public void stdRdapConformanceValidation() {
    validateSubValidation(ComplexValidation.ofRdapConformance(-12609));
  }
}
