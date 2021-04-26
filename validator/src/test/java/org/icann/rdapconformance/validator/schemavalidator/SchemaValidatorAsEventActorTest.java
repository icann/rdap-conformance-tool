package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.List;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EventActionJsonValues;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorAsEventActorTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorAsEventActorTest() {
    super(
        "rdap_entity.json",
        "/validators/asEventActor/valid.json");
    validationName = "stdRdapAsEventActorValidation";
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
    doReturn(true).when(datasets.get(EventActionJsonValues.class)).isInvalid(WRONG_ENUM_VALUE);
    validate(-11306, replaceArrayProperty("eventAction", WRONG_ENUM_VALUE),
        "The JSON string is not included as a Value with Type=\"event action\" in the RDAPJSONValues dataset.");
  }

  /**
   * 7.2.9.3.6
   */
  @Test
  public void eventDateDoesNotExist() {
    keyDoesNotExistInArray("eventDate", -11307);
  }

  /**
   * 7.2.9.3.7
   */
  @Test
  public void eventDateNotJsonString() {
    arrayItemKeyIsNotString("eventDate", -11308);
  }

  /**
   * 7.2.9.3.8
   */
  @Test
  public void eventDateNotDateTime() {
    arrayItemKeyIsNotDateTime("eventDate", -11309);
  }

  /**
   * 7.2.9.4
   */
  @Test
  public void eventActionShallAppearsOnce() {
    JSONObject firstEventWithEventAction = jsonObject.getJSONArray("asEventActor").getJSONObject(0);
    jsonObject.getJSONArray("asEventActor").put(firstEventWithEventAction);
    validate(-11310, "#/asEventActor/1/eventAction:registration", "An eventAction value exists more than once within the events array.");
  }
}
