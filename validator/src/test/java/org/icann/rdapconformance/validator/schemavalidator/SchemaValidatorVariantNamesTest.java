package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SchemaValidatorVariantNamesTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorVariantNamesTest() {
    super(
        "rdap_variant.json",
        "/validators/variantNames/valid.json");
    validationName = "stdRdapVariantsValidation";
  }

  @BeforeEach
  public void setUp() throws IOException {
    super.setUp();
    name = "variantNames";
  }

  /**
   * 7.2.11.2.6
   */
  @Test
  public void invalid() {
    invalid(-11507);
  }

  /**
   * 7.2.11.2.7.1
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-11508, List.of(
        "ldhName",
        "unicodeName"
    ));
  }

  /**
   * 7.2.11.2.7.3
   */
  @Test
  public void stdRdapLdhNameValidation() {
    stdRdapLdhNameValidation(-11510);
  }

  /**
   * 7.2.11.2.7.4
   */
  @Test
  public void stdRdapUnicodeNameValidation() {
    stdRdapUnicodeNameValidation(-11511);
  }
}
