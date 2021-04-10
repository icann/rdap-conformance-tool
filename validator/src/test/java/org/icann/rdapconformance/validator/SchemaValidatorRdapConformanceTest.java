package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorRdapConformanceTest extends SchemaValidatorTest {

  public SchemaValidatorRdapConformanceTest() {
    super("test_rdap_conformance.json",
        "/validators/rdapConformance/valid.json");
  }


  /**
   * 7.2.1.1.
   */
  @Test
  public void invalid() {
    arrayInvalid(-10500);
  }


  /**
   * 7.2.1.2.
   */
  @Test
  public void notListOfString() {
    jsonObject.put("rdapConformance", List.of(0));
    validateIsNotAJsonString(-10501, "#/rdapConformance/0:0");
  }

  /**
   * 7.2.1.3.
   */
  @Test
  public void notListOfEnum() {
    jsonObject.put("rdapConformance", List.of("wrong enum value"));
    validateNotEnum(-10502, "rdap_common.json#/definitions/rdapExtensions/allOf/1",
        "#/rdapConformance/0:wrong enum value");
  }

  /**
   * 7.2.1.4.
   */
  @Test
  public void noRdapLevel0() {
    List<String> listWithNoRdapLevel0 = List.of("icann_rdap_technical_implementation_guide_0");
    jsonObject.put("rdapConformance", listWithNoRdapLevel0);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", -10503)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/rdapConformance:[\"icann_rdap_technical_implementation_guide_0\"]")
        .hasFieldOrPropertyWithValue("message",
            "The #/rdapConformance data structure does not include rdap_level_0.");
  }
}
