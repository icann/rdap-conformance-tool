package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RoleJsonValues;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorRolesTest extends SchemaValidatorForArrayOfStringTest {

  public SchemaValidatorRolesTest() {
    super("rdap_entity.json",
        "/validators/roles/valid.json");
    validationName = "stdRdapRolesValidation";
  }

  @BeforeMethod
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
    doReturn(true).when(datasets.get(RoleJsonValues.class)).isInvalid(WRONG_ENUM_VALUE);
    notListOfEnumDataset(-11802, "The JSON string is not included as a Value with Type=\"role\".");
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
