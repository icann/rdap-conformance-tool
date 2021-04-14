package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.json.JSONObject;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class SchemaValidatorLangTest extends SchemaValidatorTest {

  public SchemaValidatorLangTest() {
    super(
        "test_rdap_lang.json",
        "/validators/lang/valid.json");
  }

  /**
   * 7.2.4.1.
   */
  @Test
  public void langViolatesLanguageTagSyntax() {
    jsonObject.put("lang", "000");
    validateRegex(-10800,
        "rdap_common.json#/definitions/lang",
        "#/lang:000");
  }
}
