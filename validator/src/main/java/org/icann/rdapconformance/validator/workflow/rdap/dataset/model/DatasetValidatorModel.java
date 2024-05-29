package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.Serializable;

public interface DatasetValidatorModel extends Serializable {
  boolean isInvalid(String subject);
}
