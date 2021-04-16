package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import java.util.List;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorAsEventActorTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorAsEventActorTest() {
    super(
        "rdap_entity.json",
        "/validators/asEventActor/valid.json");
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "asEventActor";
  }

  /**
   * 7.2.9.1.
   */
  @Test
  public void invalid() {
    invalid(-11300);
  }

  /**
   * 7.2.9.3.1
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-11302, List.of(
        "eventAction",
        "eventDate"
    ));
  }

  /**
   * 7.2.9.3.3
   */
  @Test
  public void eventActionDoesNotExist() {
    keyDoesNotExistInArray("eventAction", -11304);
  }

  /**
   * 7.2.9.3.4
   */
  @Test
  public void eventActionNotJsonString() {
    arrayItemKeyIsNotString("eventAction", -11305);
  }

  /**
   * 7.2.9.3.5
   */
  @Test
  public void eventActionNotInEnum() {
    replaceArrayProperty("eventAction", "wrong enum value");
    validateNotEnum(-11306, "rdap_asEventActor_object.json#/definitions/eventAction/allOf/1",
        "#/asEventActor/0/eventAction:wrong enum value");
  }

  /**
   * 7.2.9.3.6
   */
  @Test
  public void eventDateDoesNotExist() {
    keyDoesNotExistInArray("eventDate", -11307);
  }

  /**
   * 7.2.5.2.7
   */
  @Test
  public void eventDateNotJsonString() {
    arrayItemKeyIsNotString("eventDate", -11308);
  }

  /**
   * 7.2.5.2.8
   */
  @Test
  public void eventDateNotDateTime() {
    arrayItemKeyIsNotDateTime("eventDate", -11309);
  }
}
