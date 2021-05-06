package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

public class RegistrarId {

  private final RegistrarIds registrarIds;

  public RegistrarId(RegistrarIds registrarIds) {
    this.registrarIds = registrarIds;
  }

  public boolean contains(int registrarId) {
    return registrarIds.recordByIdentifier.containsKey(registrarId);
  }
}
