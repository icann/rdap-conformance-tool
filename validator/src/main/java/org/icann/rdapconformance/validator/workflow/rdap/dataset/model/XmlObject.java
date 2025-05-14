package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.ONE;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public abstract class XmlObject implements RDAPDatasetModel {

  private static final Logger logger = LoggerFactory.getLogger(XmlObject.class);
  private static final XPath xPath = XPathFactory.newInstance().newXPath();

  /**
   * Initialize the XML Object parser.
   */
  Document init(InputStream inputStream) throws ParserConfigurationException, IOException,
      SAXException {
    logger.info("Loading dataset " + this.getClass().getSimpleName());
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    Document document = documentBuilder.parse(inputStream);
    document.getDocumentElement().normalize(); //get the root
    return document;
  }

  public void parse(InputStream inputStream) throws Throwable {
    Document document = init(inputStream);;
  }

  /**
   * Get the value of a tag.
   */
  String getTagValue(String tag, Node node) {
    try {
      return xPath.evaluate(tag, node);
    } catch (NullPointerException | XPathExpressionException e) {
      return "";
    }
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
      String[] interval = numberOrInterval.split(DASH, 2);
      int min = Integer.parseInt(interval[ZERO]);
      int max = Integer.parseInt(interval[ONE]);
      return min <= numberToCheck || numberToCheck <= max;
    }
  }
}
