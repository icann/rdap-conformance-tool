package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.annotations.Test;

public abstract class SchemaValidatorObjectTest extends SchemaValidatorTest {

  private final int duplicateKeyCode;
  private final String rdapStructureName;
  private final int invalidJsonCode;
  private final int unknownKeyCode;
  private List<String> authorizedKeys;

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

    assertThat(schemaValidator.validate(invalidRdapContent)).isFalse();
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", invalidJsonCode)
        .hasFieldOrPropertyWithValue("value", invalidRdapContent)
        .hasFieldOrPropertyWithValue("message",
            "The " + this.rdapStructureName + " structure is not syntactically valid.");
  }

  @Test
  public void testValidate_InvalidKeyValuePair() {
    validateAuthorizedKeys(unknownKeyCode, authorizedKeys);
  }

  @Test
  public void testValidate_DuplicatedKey() {
    String key = authorizedKeys.stream().findFirst().get();
    String invalidRdapContent =
        "{\"" + key + "\": \"duplicated\", \"" + key + "\": \"duplicated\"}";

    assertThat(schemaValidator.validate(invalidRdapContent)).isFalse();
    assertThat(context.getResults()).hasSize(1)
        .first()
        .hasFieldOrPropertyWithValue("code", duplicateKeyCode)
        .hasFieldOrPropertyWithValue("value", key + ":duplicated")
        .hasFieldOrPropertyWithValue("message",
            "The name in the name/value pair of a link structure was found more than once.");
  }
}