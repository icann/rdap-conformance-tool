package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import jakarta.xml.bind.annotation.*;
import org.xml.sax.SAXException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class Ipv6AddressSpace extends XmlObject implements DatasetValidatorModel {

  @XmlElementWrapper(name = "registry", namespace = "http://www.iana.org/assignments")
  @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
  private final List<Ipv6AddressSpaceRecord> records = new ArrayList<>();

  /**
   * Read from an XML file using the DOM.
   *
   * @param inputStream InputStream object
   */
  @Override
  public void parse(InputStream inputStream)
      throws ParserConfigurationException, IOException, SAXException {
  }

  @Override
  public boolean isInvalid(String ipAddress) {
    return false;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  static class Ipv6AddressSpaceRecord {

    @XmlElement(name = "prefix", namespace = "http://www.iana.org/assignments")
    private String prefix;
    @XmlElement(name = "description", namespace = "http://www.iana.org/assignments")
    private String description;

    public Ipv6AddressSpaceRecord() {
    }

    public Ipv6AddressSpaceRecord(String prefix, String description) {
      this.prefix = prefix;
      this.description = description;
    }
  }
}
