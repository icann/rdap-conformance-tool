package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EnumDataset extends XmlObject {

  private final List<String> records = new ArrayList<>();
  private final String key;

  EnumDataset() {
    this("value");
  }

  EnumDataset(String key) {
    this.key = key;
  }

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
      records.add(transform(getTagValue(key, nodeList.item(i))));
    }
  }

  String transform(String value) {
    return value;
  }

  public List<String> getValues() {
    return records;
  }
}
