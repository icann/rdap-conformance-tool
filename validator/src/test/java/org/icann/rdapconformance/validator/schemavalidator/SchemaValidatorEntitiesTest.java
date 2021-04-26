package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorEntitiesTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorEntitiesTest() {
    super(
        "rdap_domain.json",
        "/validators/domain/valid.json");
    validationName = "stdRdapEntitiesValidation";
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "entities";
  }

  /**
   * 7.2.15.1.
   */
  @Test
  public void invalid() {
    invalid(-11900);
  }

  /**
   * 7.2.15.2
   */
  @Test
  public void stdRdapEntityLookupValidation() {
    validateSubValidation("stdRdapEntityLookupValidation", "entities", -11901);
  }
}
