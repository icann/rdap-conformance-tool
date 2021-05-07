package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

public class RegistrarIdTest {

  private RegistrarId registrarId;

  public static RegistrarId.Record getValidRecord() {
    return new RegistrarId.Record(
        292,
        "Test",
        "https://example.com/",
        ""
            + "    <record date=\"2020-11-25\" updated=\"2021-01-28\">\n"
            + "      <value>292</value>\n"
            + "      <name>Test</name>\n"
            + "      <status>Accredited</status>\n"
            + "      <rdapurl>\n"
            + "        <server>https://example.com/</server>\n"
            + "      </rdapurl>\n"
            + "    </record>");
  }

  @BeforeMethod
  public void setUp() throws IOException, ParserConfigurationException, SAXException {
    registrarId = new RegistrarId();
    registrarId.parse(RegistrarIdTest.class.getResourceAsStream("/dataset/registrar-ids.xml"));
  }

  @Test
  public void parseRegistrarIds() {
    assertThat(registrarId.recordByIdentifier.values())
        .containsExactly(
            new RegistrarId.Record(3, "Test 1", "", null),
            new RegistrarId.Record(4, "Test 2", "", null),
            new RegistrarId.Record(3878, "Test 3", "https://www.17domain.com/rdap/domain/", null),
            new RegistrarId.Record(9994, "Test 4", "", null)
        );
  }

  @Test
  public void testContainsId() {
    assertThat(registrarId.containsId(9994)).isTrue();
  }

  @Test
  public void testGetById() {
    RegistrarId.Record record = registrarId.getById(3878);
    assertThat(record).isEqualTo(
        new RegistrarId.Record(3878, "Test 3", "https://www.17domain.com/rdap/domain/", null));
  }
}