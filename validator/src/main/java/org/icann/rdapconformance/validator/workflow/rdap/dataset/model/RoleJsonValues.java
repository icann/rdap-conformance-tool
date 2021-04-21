package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class RoleJsonValues extends RDAPSubJsonValues {

  public RoleJsonValues(RDAPJsonValues rdapJsonValues) {
    super(rdapJsonValues, JsonValueType.ROLE);
  }
}
