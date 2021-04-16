package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

public class RDAPExtensions extends EnumDataset implements DatasetValidator {

  @Override
  public boolean isInvalid(String subject) {
    if (subject.equals("rdap_level_0")) {
      return false;
    }
    return !getValues().contains(subject);
  }
}
