package org.icann.rdapconformance.validator;

public enum EventAction {
  LAST_UPDATE_OF_RDAP_DATABASE("last update of RDAP database"),
  REGISTRATION("registration");

  public final String type;

  EventAction(String type) {
    this.type = type;
  }
}
