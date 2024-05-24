package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class DNSSecAlgNumbers extends XmlObject {

  private final static List<Integer> INVALID = List.of(253, 254);

  @XmlElement(name = "registry", namespace = "http://www.iana.org/assignments")
  private List<Registry> registries = new ArrayList<>();


  private List<DnsSecAlgNumbersRecord> records = new ArrayList<>();

  void afterUnmarshal(Unmarshaller u, Object parent) {
    this.records = registries.stream().flatMap(r -> r.records.stream()).collect(toList());
  }

  public boolean isValid(int number) {
    if (INVALID.contains(number)) {
      return false;
    }
    return records.stream()
        .filter(r -> r.signing.equals("Y"))
        .anyMatch(r -> numberEqualsOrInInterval(number, r.number));
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  static class Registry {

    @XmlAttribute(name = "id", namespace = "http://www.iana.org/assignments")
    private String id;

    @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
    private List<DnsSecAlgNumbersRecord> records = new ArrayList<>();

    // getters and setters
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  static class DnsSecAlgNumbersRecord {

    @XmlElement(name = "number", namespace = "http://www.iana.org/assignments")
    String number = "";  // can be a range "x-y"
    @XmlElement(name = "signing", namespace = "http://www.iana.org/assignments")
    String signing = "";

  }

}
