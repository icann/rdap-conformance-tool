package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class EPPRoid extends EnumDatasetModel<EPPRoid.EPPRoidRecord> {

  @XmlElementWrapper(name = "registry", namespace = "http://www.iana.org/assignments")
  @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
  private List<EPPRoidRecord> eppRoidRecords;

  @Override
  protected List<EPPRoidRecord> getValueRecords() {
    return this.eppRoidRecords;
  }

  @Override
  String transform(String value) {
    return value.split(",", 2)[0];
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  protected static class EPPRoidRecord implements EnumDatasetModelRecord {
    @XmlElement(name = "id", namespace = "http://www.iana.org/assignments")
    private String id;

    public String getId() {
      return id;
    }

    @Override
    public String getValue() {
      return id;
    }
  }
}
