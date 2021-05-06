package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class RegistrarIdsTest {

  @Test
  public void parseRegistrarIds() throws IOException, ParserConfigurationException, SAXException {
    RegistrarIds registrarIds = new RegistrarIds();
    registrarIds.parse(RegistrarIdsTest.class.getResourceAsStream("/dataset/registrar-ids.xml"));
    assertThat(registrarIds.records)
        .contains(
            new RegistrarIds.Record(3, "Test 1"),
            new RegistrarIds.Record(4, "Test 2"),
            new RegistrarIds.Record(3878,"Test 3"),
            new RegistrarIds.Record(9994,"Test 4")
        );
    assertThat(registrarIds.recordByIdentifier.keySet()).contains(3, 4, 3878, 9994);
    assertThat(registrarIds.names).contains("Test 1", "Test 2", "Test 3", "Test 4");
  }
}