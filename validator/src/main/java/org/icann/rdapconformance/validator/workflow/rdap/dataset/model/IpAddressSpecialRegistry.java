package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import inet.ipaddr.IPAddressString;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.w3c.dom.Node;

import java.util.List;

public abstract class IpAddressSpecialRegistry extends EnumDatasetModel<IpAddressSpecialRegistry.IpAddressSpecialRecord> {
  @XmlElementWrapper(name = "registry", namespace = "http://www.iana.org/assignments")
  @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
  private List<IpAddressSpecialRecord> ipAddressSpecialRecords;

  @Override
  protected List<IpAddressSpecialRecord> getValueRecords() {
    return this.ipAddressSpecialRecords;
  }
  public boolean isInvalid(String ip) {
    return getValues().stream().anyMatch(specialIp -> {
      IPAddressString net = new IPAddressString((String) specialIp);
      return net.contains(new IPAddressString(ip));
    });
  }

  protected static class IpAddressSpecialRecord implements EnumDatasetModelRecord {

    @XmlElement(name = "address", namespace = "http://www.iana.org/assignments")
    @XmlJavaTypeAdapter(IgnoreInnerTagAdapter.class)
    private String address;

    @Override
    public String getValue() {
      return address;
    }
  }

  protected static class IgnoreInnerTagAdapter extends XmlAdapter<Object, String> {
    @Override
    public String unmarshal(Object v) throws Exception {
      if (v instanceof Node) {
        return ((Node) v).getTextContent();
      } else {
        throw new IllegalArgumentException("Input object is not a Node");
      }
    }

    @Override
    public String marshal(String v) {
      return v;
    }
  }
}
