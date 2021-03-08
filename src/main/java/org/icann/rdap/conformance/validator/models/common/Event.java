package org.icann.rdap.conformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public class Event extends Lang {

  // eventAction -- a string denoting the reason for the event
  @JsonProperty
  // TODO action enum
  private String eventAction;

  // eventActor -- an optional identifier denoting the actor responsible for the event
  @JsonProperty
  private String eventActor;

  // eventDate -- a string containing the time and date the event occurred
  @JsonProperty
  private Instant eventDate;

  // links -- signify links to other resources on the Internet.
  @JsonProperty
  private List<Link> links;

}
