package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class VariantRelationJsonValues extends RDAPSubJsonValues {

  public VariantRelationJsonValues(RDAPJsonValues rdapJsonValues) {
    super(rdapJsonValues, JsonValueType.DOMAIN_VARIANT_RELATION);
  }
}
