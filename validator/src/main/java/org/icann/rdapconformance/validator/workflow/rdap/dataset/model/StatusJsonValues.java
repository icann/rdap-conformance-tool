package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class StatusJsonValues implements DatasetValidatorModel {

  private final RDAPJsonValues rdapJsonValues;

  public StatusJsonValues(RDAPJsonValues rdapJsonValues) {
    this.rdapJsonValues = rdapJsonValues;
  }

  @Override
  public boolean isInvalid(String subject) {
    return !rdapJsonValues.getByType(JsonValueType.STATUS).contains(subject);
  }
}
