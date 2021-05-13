package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RegistrarId extends XmlObject {

  final Map<Integer, Record> recordByIdentifier = new HashMap<>();
  final Set<String> names = new HashSet<>();

  private static String nodeToString(Node node) {
    StringWriter sw = new StringWriter();
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.transform(new DOMSource(node), new StreamResult(sw));
    } catch (TransformerException te) {
      System.out.println("nodeToString Transformer Exception");
    }
    return sw.toString();
  }

  @Override
  public void parse(InputStream inputStream)
      throws IOException, SAXException, ParserConfigurationException {
    Document document = this.init(inputStream);
    NodeList nodeList = document.getElementsByTagName("record");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      Record record = new Record(
          Integer.parseInt(getTagValue("value", node)),
          getTagValue("name", node),
          getTagValue("rdapurl/server", node),
          nodeToString(node));
      recordByIdentifier.put(record.value, record);
      names.add(record.name);
    }
  }

  public boolean containsId(int registrarId) {
    return recordByIdentifier.containsKey(registrarId);
  }

  public Record getById(int registrarId) {
    return recordByIdentifier.get(registrarId);
  }

  public static class Record {

    private final int value;
    private final String name;
    private final String xmlRepresentation;
    private final String rdapUrl;

    public Record(int value, String name, String rdapUrl, String xmlRepresentation) {
      this.value = value;
      this.name = name;
      this.rdapUrl = rdapUrl;
      this.xmlRepresentation = xmlRepresentation;
    }

    public int getValue() {
      return value;
    }

    public String getName() {
      return name;
    }

    public String getRdapUrl() {
      return rdapUrl;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Record record = (Record) o;
      return value == record.value && name.equals(record.name) && Objects
          .equals(rdapUrl, record.rdapUrl);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value, name, rdapUrl);
    }

    @Override
    public String toString() {
      return xmlRepresentation;
    }
  }
}
