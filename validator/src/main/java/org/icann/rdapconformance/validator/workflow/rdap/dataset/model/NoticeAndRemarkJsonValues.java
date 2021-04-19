package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class NoticeAndRemarkJsonValues implements DatasetValidatorModel {

  private final RDAPJsonValues rdapJsonValues;

  public NoticeAndRemarkJsonValues(RDAPJsonValues rdapJsonValues) {
    this.rdapJsonValues = rdapJsonValues;
  }

  @Override
  public boolean isInvalid(String subject) {
    return !rdapJsonValues.getByType(JsonValueType.NOTICE_AND_REMARK_TYPE).contains(subject);
  }
}
