package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RDAPJsonValues extends XmlObject {

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
      records.add(new Record(JsonValueType
          .valueOf(getTagValue("type", nodeList.item(i)).toUpperCase().replace(" ", "_")),
          getTagValue("value", nodeList.item(i))));
    }
  }

  public Set<String> getByType(JsonValueType type) {
    return records.stream()
        .filter(r -> r.type.equals(type))
        .map(Record::getValue)
        .collect(Collectors.toSet());
  }

  public enum JsonValueType {
    NOTICE_AND_REMARK_TYPE,
    STATUS,
    EVENT_ACTION,
    ROLE,
    DOMAIN_VARIANT_RELATION,
    REDACTED_EXPRESSION_LANGUAGE,
  }

  private static class Record {

    private final JsonValueType type;
    private final String value;

    public Record(JsonValueType type, String value) {
      this.type = type;
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
