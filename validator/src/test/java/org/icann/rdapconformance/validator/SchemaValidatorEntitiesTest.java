package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorEntitiesTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorEntitiesTest() {
    super(
        "rdap_domain.json",
        "/validators/domain/valid.json");
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
