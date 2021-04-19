package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.testng.annotations.Test;

public class SchemaValidatorRdapConformanceTest extends SchemaValidatorForArrayOfStringTest {

  public SchemaValidatorRdapConformanceTest() {
    super("test_rdap_conformance.json",
        "/validators/rdapConformance/valid.json");
  }


  /**
   * 7.2.1.1.
   */
  @Test
  public void invalid() {
    invalid(-10500);
  }


  /**
   * 7.2.1.2.
   */
  @Test
  public void notListOfString() {
    notListOfString(-10501);
  }

  /**
   * 7.2.1.3.
   */
  @Test
  public void notListOfEnum() {
    doReturn(true).when(datasetService.get(RDAPExtensions.class)).isInvalid(any());
    validate(-10502, wrongEnum(),
        "The JSON string is not included as an Extension Identifier in RDAPExtensions.");
  }

  /**
   * 7.2.1.4.
   */
  @Test
  public void noRdapLevel0() {
    List<String> listWithNoRdapLevel0 = List.of("icann_rdap_technical_implementation_guide_0");
    jsonObject.put("rdapConformance", listWithNoRdapLevel0);
    Assertions.assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();
    assertThat(results.getAll())
        .filteredOn("code", -10503)
        .first()
        .hasFieldOrPropertyWithValue("value", "#/rdapConformance:[\"icann_rdap_technical_implementation_guide_0\"]")
        .hasFieldOrPropertyWithValue("message",
            "The #/rdapConformance data structure does not include rdap_level_0.");
  }
}
