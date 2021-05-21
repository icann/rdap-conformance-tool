package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EventActionJsonValues;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorEventsTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorEventsTest() {
    super(
        "test_rdap_events.json",
        "/validators/events/valid.json");
    validationName = "stdRdapEventsValidation";
  }

  /**
   * 7.2.5.1.
   */
  @Test
  public void invalid() {
    invalid(-10900);
  }

  /**
   * 7.2.5.2.1
   */
  @Test
  public void unauthorizedKey() {
    validateArrayAuthorizedKeys(-10901, List.of(
        "eventAction",
        "eventActor",
        "eventDate",
        "links"
    ));
  }

  /**
   * 7.2.5.2.3
   */
  @Test
  public void eventActionDoesNotExist() {
    keyDoesNotExistInArray("eventAction", -10903);
  }

  /**
   * 7.2.5.2.4
   */
  @Test
  public void eventActionNotJsonString() {
    arrayItemKeyIsNotString("eventAction", -10904);
  }

  /**
   * 7.2.5.2.5
   */
  @Test
  public void eventActionNotInEnum() {
    doReturn(true).when(datasets.get(EventActionJsonValues.class))
        .isInvalid(WRONG_ENUM_VALUE);
    validate(-10905, replaceArrayProperty("eventAction", WRONG_ENUM_VALUE),
        "The JSON string is not included as a Value with Type=\"event action\" in the "
            + "RDAPJSONValues data set.");
  }

  /**
   * 7.2.5.2.6
   */
  @Test
  public void eventDateDoesNotExist() {
    keyDoesNotExistInArray("eventDate", -10906);
  }

  /**
   * 7.2.5.2.7
   */
  @Test
  public void eventDateNotJsonString() {
    arrayItemKeyIsNotString("eventDate", -10907);
  }

  /**
   * 7.2.5.2.8
   */
  @Test
  public void eventDateNotDateTime() {
    arrayItemKeyIsNotDateTime("eventDate", -10908);
  }

  /**
   * 7.2.5.2.9
   */
  @Test
  public void eventActorNotDateTime() {
    arrayItemKeyIsNotString("eventActor", -10909);
  }

  /**
   * 7.2.5.2.10.
   */
  @Test
  public void linksWithNoEventActor() {
    replaceArrayProperty("links", 0);
    Assertions.assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll()).filteredOn(r -> r.getCode() == -10910)
        .hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("value", "{\"links\":0,\"eventAction\":\"registration\",\"eventDate\":\"1997-09-15T04:00:00Z\"}")
        .hasFieldOrPropertyWithValue("message",
            "A links structure was found but an eventActor was not.");
  }

  /**
   * 7.2.5.2.11
   */
  @Test
  public void linksViolatesLinksValidation() {
    linksViolatesLinksValidation(-10911);
  }

  /**
   * 7.2.5.3
   */
  @Test
  public void eventActionShallAppearsOnce() {
    JSONObject firstEventWithEventAction = jsonObject.getJSONArray("events").getJSONObject(0);
    jsonObject.getJSONArray("events").put(firstEventWithEventAction);
    validate(-10912, "#/events/1/eventAction:registration", "An eventAction value exists more than once within the events array.");
  }

  /**
   * 7.2.5.3
   */
  @Test
  public void eventActionCanAppearTwiceInTwoDistinctEvents() {
    jsonObject.put("someOtherObject", new JSONObject(jsonObject.toString()));
    assertThat(schemaValidator.validate(jsonObject.toString())).isTrue();
    assertThat(results.getGroupOk()).contains(validationName);
  }
}
