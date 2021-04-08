package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.testng.annotations.Test;

public class SchemaValidatorRdapConformanceTest extends SchemaValidatorTest {

  public SchemaValidatorRdapConformanceTest() {
    super("rdap_response_common.json",
        "/validators/rdapConformance/valid.json");
  }

  @Test
  public void notListOfString() {
    jsonObject.put("rdapConformance", List.of(0));
    validateIsNotAJsonString(-10501, "#/rdapConformance/0:0");
  }

  @Test
  public void notListOfEnum() {
    jsonObject.put("rdapConformance", List.of("wrong enum value"));
    validateNotEnum(-10502, "rdap_common.json#/definitions/rdapExtensions/allOf/1",
        "#/rdapConformance/0:wrong enum value");
  }

  @Test
  public void noRdapLevel0() {
    List<String> listWithNoRdapLevel0 = List.of("icann_rdap_technical_implementation_guide_0");
    jsonObject.put("rdapConformance", listWithNoRdapLevel0);
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(context.getResults())
        .filteredOn("code", -10503)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/rdapConformance:[\"icann_rdap_technical_implementation_guide_0\"]")
        .hasFieldOrPropertyWithValue("message",
            "The #/rdapConformance data structure does not include rdap_level_0.");
  }
}
