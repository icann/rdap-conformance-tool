package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class XmlObject implements RDAPDatasetModel {

  /**
   * Initialize the XML Object parser.
   */
  Document init(InputStream inputStream) throws ParserConfigurationException, IOException,
      SAXException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(inputStream);
    document.getDocumentElement().normalize(); //get the root
    return document;
  }

  /**
   * Get the value of a tag.
   */
  String getTagValue(String tag, Node node) {
    Element element = (Element) node;
    NodeList nodeList;
    try {
      nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
    } catch (NullPointerException e) {
      return "";
    }
    if (nodeList.getLength() > 0) {
      return nodeList.item(0).getNodeValue().strip();
    }
    return "";
  }

  /**
   * Get an attribute value.
   */
  String getAttribute(String attribute, Node node) {
    Element element = (Element) node;
    String value = element.getAttribute(attribute);
    if (value == null) {
      return "";
    }
    return value.strip();
  }


  protected boolean numberEqualsOrInInterval(int numberToCheck, String numberOrInterval) {
    try {
      int nbr = Integer.parseInt(numberOrInterval);
      return nbr == numberToCheck;
    } catch (NumberFormatException e) {
      String[] interval = numberOrInterval.split("-", 2);
      int min = Integer.parseInt(interval[0]);
      int max = Integer.parseInt(interval[1]);
      return min <= numberToCheck || numberToCheck <= max;
    }
  }
}
