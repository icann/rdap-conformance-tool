package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class EventActionJsonValues implements DatasetValidatorModel {

  private final RDAPJsonValues rdapJsonValues;

  public EventActionJsonValues(RDAPJsonValues rdapJsonValues) {
    this.rdapJsonValues = rdapJsonValues;
  }

  @Override
  public boolean isInvalid(String subject) {
    return !rdapJsonValues.getByType(JsonValueType.EVENT_ACTION).contains(subject);
  }
}
