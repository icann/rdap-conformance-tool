package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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
