package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorVariantsTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorVariantsTest() {
    super(
        "test_rdap_variants.json",
        "/validators/variants/valid.json");
  }

  /**
   * 7.2.11.1.
   */
  @Test
  public void invalid() {
    invalid(-11500);
  }

  /**
   * 7.2.11.2.1
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-11501, List.of(
        "idnTable",
        "relation",
        "variantNames"
    ));
  }

  /**
   * 7.2.11.2.5
   */
  @Test
  public void idnTableNotJsonString() {
    replaceArrayProperty("idnTable", 0);
    validateIsNotAJsonString(-11506, "#/variants/0/idnTable:0");
  }
}
