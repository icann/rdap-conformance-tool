package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class RDAPExtensions extends ValueAttributeDatasetModel implements DatasetValidatorModel {

  @Override
  public boolean isInvalid(String subject) {
    if (subject.equals("rdap_level_0")) {
      return false;
    }
    return !getValues().contains(subject);
  }
}
