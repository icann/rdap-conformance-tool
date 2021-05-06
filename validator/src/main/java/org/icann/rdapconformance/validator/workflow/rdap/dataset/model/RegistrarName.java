package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

public class RegistrarName implements DatasetValidatorModel {

  private final RegistrarIds registrarIds;

  public RegistrarName(RegistrarIds registrarIds) {
    this.registrarIds = registrarIds;
  }

  @Override
  public boolean isInvalid(String subject) {
    return !registrarIds.names.contains(subject);
  }
}
