package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import inet.ipaddr.IPAddressString;
import jakarta.xml.bind.annotation.*;
import org.xml.sax.SAXException;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class Ipv6AddressSpace extends XmlObject implements DatasetValidatorModel {

  private static final String GLOBAL_UNICAST = "Global Unicast";

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

  public List<Ipv6AddressSpaceRecord> getRecords() {
    return records;
  }

  @Override
  public boolean isInvalid(String ipAddress) {
    // Return true (invalid) if the IPv6 address is NOT in Global Unicast address space
    // According to spec 7.1.2.2, IPv6 address MUST be part of "Global Unicast" allocation
    return records.stream()
        .filter(r -> GLOBAL_UNICAST.equals(r.getDescription()))
        .noneMatch(r -> {
          try {
            IPAddressString net = new IPAddressString(r.getPrefix());
            return net.contains(new IPAddressString(ipAddress));
          } catch (Exception e) {
            // If there's an error parsing the address or prefix, treat as invalid
            return false;
          }
        });
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Ipv6AddressSpaceRecord {

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

    public String getPrefix() {
      return prefix;
    }

    public String getDescription() {
      return description;
    }
  }
}
