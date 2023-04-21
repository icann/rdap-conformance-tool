package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

public class SchemaValidatorVcardArrayInDomainTest extends SchemaValidatorDomainTest {

  @Test
  public void testVcardWrongCategory() throws IOException {
    replaceVcardArray("/validators/vcardArray/wrongCategory.json");
    // #/entities/0/vcardArray/1/1/0:
    validateWithoutGroupTests(-12305,
        "#/entities/0/vcardArray/1/3:wrong-category",
        "unknown vcard category: \"wrong-category\".");
    assertThat(results.getAll())
        .filteredOn("value", "version")
        .isEmpty();
  }

  @Test
  public void testFnMissing() throws IOException {
    replaceVcardArray("/validators/vcardArray/fnMissing.json");
    validateWithoutGroupTests(-12305,
        "#/entities/0/vcardArray/1:" + jsonObject.query("#/entities/0/vcardArray/1"),
        "The value for the JSON name value is not a syntactically valid vcardArray.");
  }

  @Test
  public void testContactUri() throws IOException {
    replaceVcardArray("/validators/vcardArray/contactUriValid.json");
    schemaValidator.validate(jsonObject.toString());
    assertThat(results.getAll())
            .doesNotContain(RDAPValidationResult.builder()
                    .code(-12305)
                    .value("#/entities/0/vcardArray/1:" + jsonObject.query("#/entities/0/vcardArray/1"))
                    .message("The value for the JSON name value is not a syntactically valid vcardArray.")
                    .build());
  }
  @Test
  public void testVcardDoesNotContainsProperty() throws IOException {
    replaceVcardArray("/validators/vcardArray/trivialArray.json");
    validateWithoutGroupTests(-12305,
        "#/entities/0/vcardArray:" + jsonObject.query("#/entities/0/vcardArray"),
        "The value for the JSON name value is not a syntactically valid vcardArray.");
  }

  private void replaceVcardArray(String wrongVcardPath) throws IOException {
    JSONArray vcardArrayWithWrongCategory = new JSONObject(getResource(
        wrongVcardPath)).getJSONArray("vcardArray");
    jsonObject
        .getJSONArray("entities")
        .getJSONObject(0)
        .put("vcardArray", vcardArrayWithWrongCategory);
  }
}