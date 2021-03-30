package org.icann.rdapconformance.validator;

import java.util.List;

public class SchemaValidatorHelpTest extends SchemaValidatorTest {

  public SchemaValidatorHelpTest() {
    super(
        "help",
        "rdap_help.json",
        "/validators/help/valid.json",
        -12500,
        -12501,
        -12502,
        -12503,
        -12505, List.of("notices", "rdapConformance", "lang")
        );
  }
}