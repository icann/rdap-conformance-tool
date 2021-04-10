package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorEventsTest extends SchemaValidatorForArrayTest {

  public SchemaValidatorEventsTest() {
    super(
        "test_rdap_events.json",
        "/validators/events/valid.json");
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
    replaceArrayProperty("eventAction", "wrong enum value");
    validateNotEnum(-10905, "rdap_event.json#/definitions/eventAction/allOf/1",
        "#/events/0/eventAction:wrong enum value");
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
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
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
}
