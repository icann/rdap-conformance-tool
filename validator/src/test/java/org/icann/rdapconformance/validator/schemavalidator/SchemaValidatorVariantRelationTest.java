package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.VariantRelationJsonValues;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorVariantRelationTest extends SchemaValidatorForArrayOfStringTest {

  public SchemaValidatorVariantRelationTest() {
    super(
        "rdap_variant.json",
        "/validators/relation/valid.json");
    validationName = "stdRdapVariantsValidation";
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
    doReturn(true).when(datasets.get(VariantRelationJsonValues.class)).isInvalid(WRONG_ENUM_VALUE);
    notListOfEnumDataset(-11505,
        "The JSON string is not included as a Value with Type=\"domain variant relation\".");
  }
}
