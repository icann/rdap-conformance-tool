package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import jakarta.xml.bind.Unmarshaller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;


public abstract class EnumDatasetModel<T extends EnumDatasetModelRecord> extends XmlObject implements DatasetValidatorModel {

  private Set<String> records = new HashSet<>();

  public void afterUnmarshal(Unmarshaller u, Object parent) {
    this.records = getValueRecords().stream().map(r -> transform(r.getValue())).collect(toSet());
  }

  protected abstract List<T> getValueRecords();

  String transform(String value) {
    return value;
  }

  public Set<String> getValues() {
    return records;
  }

  @Override
  public boolean isInvalid(String subject) {
    return !getValues().contains(subject);
  }
}
