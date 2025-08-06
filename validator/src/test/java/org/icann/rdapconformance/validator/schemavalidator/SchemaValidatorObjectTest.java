package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public abstract class SchemaValidatorObjectTest extends SchemaValidatorTest {

  private final int duplicateKeyCode;
  private final String rdapStructureName;
  private final int invalidJsonCode;
  private final int unknownKeyCode;
  private final List<String> authorizedKeys;

  public SchemaValidatorObjectTest(
      String rdapStructureName,
      String schemaName,
      String validJson,
      int invalidJsonCode,
      int unknownKeyCode,
      int duplicateKeyCode,
      List<String> authorizedKeys) {
    super(schemaName,
        validJson);
    this.rdapStructureName = rdapStructureName;
    this.invalidJsonCode = invalidJsonCode;
    this.unknownKeyCode = unknownKeyCode;
    this.duplicateKeyCode = duplicateKeyCode;
    this.authorizedKeys = new ArrayList<>(authorizedKeys);
    Collections.sort(this.authorizedKeys);
  }

  @Test
  public void testValidate_InvalidJson() {
    String invalidRdapContent = "{\"invalid-json\": \"with trailing comma\" ]";

    Assertions.assertThat(schemaValidator.validate(invalidRdapContent)).isFalse();
    assertThat(results.getAll()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", invalidJsonCode)
        .hasFieldOrPropertyWithValue("value", invalidRdapContent)
        .hasFieldOrPropertyWithValue("message",
            "The " + this.rdapStructureName + " structure is not syntactically valid.");
  }

  /*
    Ignoring test due to new cache implementation where new JSONException is handling duplicates
   */
  @Test
  @Ignore
  public void testValidate_DuplicatedKey() {
    String key = authorizedKeys.stream().findFirst().get();
    String invalidRdapContent =
        "{\"" + key + "\": \"duplicated\", \"" + key + "\": \"duplicated\"}";

    Assertions.assertThat(schemaValidator.validate(invalidRdapContent)).isFalse();
    assertThat(results.getAll()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", duplicateKeyCode)
        .hasFieldOrPropertyWithValue("value", key + ":duplicated")
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair of a link structure was found more than once.");
  }

  protected void validateIsNotAJsonString(int errorCode, String key) {
    jsonObject.put(key, 0);
    super.validateIsNotAJsonString(errorCode, "#/" + key + ":0");
  }

  protected void validateIsNotANumber(int errorCode, String key) {
    jsonObject.put(key, "not-a-number");
    super.validateIsNotANumber(errorCode, "#/" + key + ":not-a-number");
  }

  protected void noticesNotInTopMost(int errorCode) {
    jsonObject.getJSONArray("entities").getJSONObject(0).put("notices", jsonObject.get("notices"));
    validate(errorCode,
        "#/entities/0/notices:" + jsonObject.get("notices"),
        "The value for the JSON name notices exists but " + name + " object is "
            + "not the topmost JSON object.");
  }
}