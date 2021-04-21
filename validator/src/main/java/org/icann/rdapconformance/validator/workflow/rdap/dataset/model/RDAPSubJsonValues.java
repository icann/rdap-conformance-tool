package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class RDAPSubJsonValues implements DatasetValidatorModel {

  private final RDAPJsonValues rdapJsonValues;
  private final JsonValueType jsonValueType;

  public RDAPSubJsonValues(RDAPJsonValues rdapJsonValues, JsonValueType jsonValueType) {
    this.rdapJsonValues = rdapJsonValues;
    this.jsonValueType = jsonValueType;
  }

  @Override
  public boolean isInvalid(String subject) {
    return !rdapJsonValues.getByType(jsonValueType).contains(subject);
  }
}
