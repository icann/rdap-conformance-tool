package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DsRrTypes extends XmlObject {

  protected final List<Record> records = new ArrayList<>();

  /**
   * Read from an XML file stream using the DOM.
   */
  @Override
  public void parse(InputStream inputStream)
      throws ParserConfigurationException, IOException, SAXException {
    Document document = this.init(inputStream);
    Element element = (Element) document.getElementsByTagName("registry").item(0);
    NodeList nodeList = element.getElementsByTagName("record");
    for (int i = 0; i < nodeList.getLength(); i++) {
      this.records.add(new Record(getTagValue("value", nodeList.item(i)),
          getTagValue("status", nodeList.item(i))));
    }
  }

  public boolean isAssigned(int number) {
    return this.records.stream()
        .filter(r -> !r.status.equals("-"))
        .anyMatch(r -> numberEqualsOrInInterval(number, r.value));
  }

  private static class Record {

    private final String value;
    private final String status;

    public Record(String value, String status) {
      this.value = value;
      this.status = status;
    }
  }
}
