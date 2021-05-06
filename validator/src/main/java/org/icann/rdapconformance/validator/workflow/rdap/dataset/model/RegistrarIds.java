package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RegistrarIds extends XmlObject {

  final List<Record> records = new ArrayList<>();
  final Map<Integer, Record> recordByIdentifier = new HashMap<>();
  final Set<String> names = new HashSet<>();

  @Override
  public void parse(InputStream inputStream)
      throws IOException, SAXException, ParserConfigurationException {
    Document document = this.init(inputStream);
    NodeList nodeList = document.getElementsByTagName("record");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Record record = new Record(
          Integer.parseInt(getTagValue("value", nodeList.item(i))),
          getTagValue("name", nodeList.item(i)));
      records.add(record);
      recordByIdentifier.put(record.value, record);
      names.add(record.name);
    }
  }

  static class Record {

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Record record = (Record) o;
      return value == record.value && name.equals(record.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value, name);
    }

    private final int value;
    private final String name;

    public Record(int value, String name) {
      this.value = value;
      this.name = name;
    }

    public int getValue() {
      return value;
    }

    public String getName() {
      return name;
    }
  }
}
