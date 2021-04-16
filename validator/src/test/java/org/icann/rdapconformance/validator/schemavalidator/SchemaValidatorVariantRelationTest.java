package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorVariantRelationTest extends SchemaValidatorForArrayOfStringTest {

  public SchemaValidatorVariantRelationTest() {
    super(
        "rdap_variant.json",
        "/validators/relation/valid.json");
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "relation";
  }

  /**
   * 7.2.11.2.3
   */
  @Test
  public void invalid() {
    invalid(-11503);
  }

  /**
   * 7.2.11.2.4.1
   */
  @Test
  public void notListOfString() {
    notListOfString(-11504);
  }

  /**
   * 7.2.11.2.4.2
   */
  @Test
  public void notListOfEnum() {
    notListOfEnum(-11505, "#/definitions/variantRelation/allOf/1");
  }
}
