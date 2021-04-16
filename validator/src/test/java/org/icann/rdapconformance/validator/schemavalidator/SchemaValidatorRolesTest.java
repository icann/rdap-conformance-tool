package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorRolesTest extends SchemaValidatorForArrayOfStringTest {

  public SchemaValidatorRolesTest() {
    super("rdap_entity.json",
        "/validators/roles/valid.json");
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "roles";
  }

  /**
   * 7.2.14.1.
   */
  @Test
  public void invalid() {
    invalid(-11800);
  }


  /**
   * 7.2.14.2.
   */
  @Test
  public void notListOfString() {
    notListOfString(-11801);
  }

  /**
   * 7.2.14.3.
   */
  @Test
  public void notListOfEnum() {
    notListOfEnum(-11802, "#/definitions/entityRole/allOf/1");
  }

  /**
   * 7.2.14.4.
   */
  @Test
  public void duplicates() {
    jsonObject.put("roles", List.of("registrar", "registrar"));
    validate(-11803, "{\"roles\":[\"registrar\",\"registrar\"]}", "A #/roles value appeared more than once.");
  }
}
