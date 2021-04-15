package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Ipv6AddressSpace extends XmlObject {

  private final List<Record> records = new ArrayList<>();

  /**
   * Read from an XML file using the DOM.
   *
   * @param inputStream InputStream object
   */
  @Override
  public void parse(InputStream inputStream)
      throws ParserConfigurationException, IOException, SAXException {
    Document document = this.init(inputStream);
    NodeList nodeList = document.getElementsByTagName("record");
    for (int i = 0; i < nodeList.getLength(); i++) {
      this.records.add(new Record(getTagValue("prefix", nodeList.item(i)),
          getTagValue("description", nodeList.item(i))));
    }
  }

  public boolean isValid(String ipAddress) {
    throw new RuntimeException("To be implemented");
  }

  private static class Record {

    private final String prefix;
    private final String description;

    public Record(String prefix, String description) {
      this.prefix = prefix;
      this.description = description;
    }
  }
}
