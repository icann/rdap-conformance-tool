package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DNSSecAlgNumbers extends XmlObject {

  private final static List<Integer> INVALID = List.of(253, 254);
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
      records.add(new Record(getTagValue("number", nodeList.item(i)),
          getTagValue("signing", nodeList.item(i))));
    }
  }

  public boolean isValid(int number) {
    if (INVALID.contains(number)) {
      return false;
    }
    return records.stream()
        .filter(r -> r.signing.equals("Y"))
        .anyMatch(r -> numberEqualsOrInInterval(number, r.number));
  }

  private static class Record {

    private final String number;  // can be a range "x-y"
    private final String signing;

    public Record(String number, String signing) {
      this.number = number;
      this.signing = signing;
    }
  }

}
