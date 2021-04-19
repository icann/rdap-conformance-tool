package org.icann.rdapconformance.validator.customvalidator;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;

public class MediaTypesValidator extends DatasetValidator {

  public MediaTypesValidator(MediaTypes mediaTypes) {
    super(mediaTypes, "mediaTypes");
  }

  @Override
  protected String getErrorMsg() {
    return "The JSON value is not included as a Name in mediaTypes.";
  }
}
