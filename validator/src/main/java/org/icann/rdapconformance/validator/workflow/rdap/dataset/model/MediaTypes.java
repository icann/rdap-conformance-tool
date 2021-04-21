package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MediaTypes extends XmlObject implements DatasetValidatorModel {

  private final Set<String> records = new HashSet<>();

  /**
   * Read from an XML file using the DOM.
   *
   * @param inputStream InputStream object
   */
  @Override
  public void parse(InputStream inputStream)
      throws IOException, SAXException, ParserConfigurationException {
    Document document = this.init(inputStream);
    NodeList registryList = document.getElementsByTagName("registry");
    for (int i = 0; i < registryList.getLength(); i++) {
      Node registry = registryList.item(i);
      String registryId = getAttribute("id", registry);
      if (registryId.equals("media-types")) {
        // skip main registry
        continue;
      }
      Element element = (Element) registry;
      NodeList recordList = element.getElementsByTagName("record");
      for (int j = 0; j < recordList.getLength(); j++) {
        records.add(registryId + "/" + getTagValue("name", recordList.item(j)));
      }
    }
  }

  @Override
  public boolean isInvalid(String subject) {
    return !records.contains(subject);
  }
}
