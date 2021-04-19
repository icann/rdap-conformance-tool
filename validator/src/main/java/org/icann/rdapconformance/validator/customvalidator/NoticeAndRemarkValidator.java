package org.icann.rdapconformance.validator.customvalidator;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DatasetValidatorModel;

public class NoticeAndRemarkValidator extends DatasetValidator {

  public NoticeAndRemarkValidator(
      DatasetValidatorModel datasetValidatorModel) {
    super(datasetValidatorModel, "notice-and-remark");
  }

  @Override
  protected String getErrorMsg() {
    return "The JSON string is not included as a Value with Type=\"notice  and remark type\" in the RDAPJSONValues dataset.";
  }
}
