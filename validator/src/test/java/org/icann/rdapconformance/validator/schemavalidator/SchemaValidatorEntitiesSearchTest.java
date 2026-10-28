package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class SchemaValidatorEntitiesSearchTest extends SchemaValidatorTest {

  public SchemaValidatorEntitiesSearchTest() {
    super("rdap_entities_search.json", "/validators/entities/valid.json");
    validationName = "stdRdapEntitiesSearchValidation";
  }

  @Test
  public void testValidate_NotAnObject_Returns13001() {
    assertThat(schemaValidator.validate("[]")).isFalse();
    assertThat(results.getAll()).anyMatch(r -> r.getCode() == -13001);
  }

  @Test
  public void testValidate_MissingEntitySearchResults_StillStructurallyValid() {
    jsonObject.remove("entitySearchResults");
    assertThat(schemaValidator.validate(jsonObject.toString())).isTrue();
  }
}
