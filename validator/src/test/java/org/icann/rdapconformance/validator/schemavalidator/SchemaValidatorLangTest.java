package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class SchemaValidatorLangTest extends SchemaValidatorTest {

  public SchemaValidatorLangTest() {
    super(
        "test_rdap_lang.json",
        "/validators/lang/valid.json");
    validationName = "stdRdapLanguageIdentifierValidation";
  }

  /**
   * 7.2.4.1.
   */
  @Test
  public void langViolatesLanguageTagSyntax() {
    jsonObject.put("lang", "000");
    validateRegex(-10800,
        "rdap_common.json#/definitions/lang",
        "#/lang:000");
  }
}
