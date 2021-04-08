package org.icann.rdapconformance.validator;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorHelpTest extends SchemaValidatorObjectTest {

  public SchemaValidatorHelpTest() {
    super(
        "help",
        "rdap_help.json",
        "/validators/help/valid.json",
        -12500,
        -12501,
        -12502,
        List.of("notices", "rdapConformance", "lang")
    );
  }

  @Test
  public void stdRdapNoticesRemarksValidation() {
    validateSubValidation(ComplexValidation.ofNotices(-12503));
  }

  @Test
  public void stdRdapConformanceValidation() {
    validateSubValidation(ComplexValidation.ofRdapConformance(-12505));
  }
}