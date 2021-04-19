package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import inet.ipaddr.IPAddressString;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Ipv4AddressSpace extends XmlObject implements DatasetValidatorModel {

  public List<Record> getRecords() {
    return records;
  }

  private final List<Record> records = new ArrayList<>();

  /**
   * Read from an XML file using the DOM.
   *
   * @param inputStream InputStream object
   */
  @Override
  public void parse(InputStream inputStream)
      throws IOException, SAXException, ParserConfigurationException {
    Document document = this.init(inputStream);
    NodeList nodeList = document.getElementsByTagName("record");
    for (int i = 0; i < nodeList.getLength(); i++) {
      records.add(new Record(String.format("%d.0.0.0/8",
          Integer.valueOf(getTagValue("prefix", nodeList.item(i)).split("/")[0])),
          getTagValue("status", nodeList.item(i))));
    }
  }

  public boolean isInvalid(String ipAddress) {
    return records.stream()
        .filter(r -> r.getStatus().equals("ALLOCATED") || r.getStatus().equals("LEGACY"))
        .noneMatch(r -> {
          IPAddressString net = new IPAddressString(r.getPrefix());
          return net.contains(new IPAddressString(ipAddress));
        });
  }

  public static class Record {

    private final String prefix;

    public String getPrefix() {
      return prefix;
    }

    public String getStatus() {
      return status;
    }

    private final String status;

    public Record(String prefix, String status) {
      this.prefix = prefix;
      this.status = status;
    }
  }
}
