package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class DsRrTypes extends XmlObject {

  @XmlElementWrapper(name = "registry", namespace = "http://www.iana.org/assignments")
  @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
  protected final List<Record> records = new ArrayList<>();

  public boolean isAssigned(int number) {
    return this.records.stream()
        .filter(r -> !r.status.equals("-"))
        .anyMatch(r -> numberEqualsOrInInterval(number, r.value));
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  private static class Record {

    @XmlElement(name = "value", namespace = "http://www.iana.org/assignments")
    private String value;
    @XmlElement(name = "status", namespace = "http://www.iana.org/assignments")
    private String status;
  }
}
