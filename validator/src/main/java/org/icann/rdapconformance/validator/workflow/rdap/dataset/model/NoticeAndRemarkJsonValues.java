package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class NoticeAndRemarkJsonValues extends RDAPSubJsonValues {

  public NoticeAndRemarkJsonValues(RDAPJsonValues rdapJsonValues) {
    super(rdapJsonValues, JsonValueType.NOTICE_AND_REMARK_TYPE);
  }
}
