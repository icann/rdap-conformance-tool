package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class StatusJsonValues extends RDAPSubJsonValues {

  public StatusJsonValues(RDAPJsonValues rdapJsonValues) {
    super(rdapJsonValues, JsonValueType.STATUS);
  }
}
