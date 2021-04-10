package org.icann.rdapconformance.validator;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorPublicIdsTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorPublicIdsTest() {
    super(
        "test_rdap_publicIds.json",
        "/validators/publicIds/valid.json");
  }

  /**
   * 7.2.8.1.
   */
  @Test
  public void invalid() {
    arrayInvalid(-11200);
  }

  /**
   * 7.2.8.2.1
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-11201, List.of(
        "identifier",
        "type"
    ));
  }

  /**
   * 7.2.8.2.3
   */
  @Test
  public void typeDoesNotExist() {
    keyDoesNotExistInArray("type", -11203);
  }

  /**
   * 7.2.8.2.3
   */
  @Test
  public void identifierDoesNotExist() {
    keyDoesNotExistInArray("identifier", -11203);
  }

  /**
   * 7.2.8.2.4
   */
  @Test
  public void typeNotJsonString() {
    arrayItemKeyIsNotString("type", -11204);
  }

  /**
   * 7.2.8.2.5
   */
  @Test
  public void identifierNotJsonString() {
    arrayItemKeyIsNotString("identifier", -11205);
  }
}
