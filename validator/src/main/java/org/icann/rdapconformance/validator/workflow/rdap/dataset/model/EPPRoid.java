package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

public class EPPRoid extends EnumDataset {

  public EPPRoid() {
    super("id");
  }

  @Override
  String transform(String value) {
    return value.split(",", 2)[0];
  }
}
