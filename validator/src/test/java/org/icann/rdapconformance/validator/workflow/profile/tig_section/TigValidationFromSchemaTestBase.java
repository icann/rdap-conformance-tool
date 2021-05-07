package org.icann.rdapconformance.validator.workflow.profile.tig_section;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.jayway.jsonpath.JsonPath;
import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONObject;

public abstract class TigValidationFromSchemaTestBase extends SchemaValidatorTest {

  protected final String validationName;

  public TigValidationFromSchemaTestBase(
      String schemaName,
      String validJson,
      String validationName) {
    super(schemaName, validJson);
    this.validationName = validationName;
  }

  public abstract ProfileJsonValidation getTigValidation();

  @Override
  public void testValidate_ok() {
    assertThat(getTigValidation().validate()).isTrue();
    assertThat(results.getGroupOk()).containsExactly(validationName);
    assertThat(getTigValidation().doLaunch()).isTrue();
  }

  @Override
  protected void validate(int errorCode, String value, String msg) {
    assertThat(getTigValidation().validate()).isFalse();
    assertThat(results.getAll())
        .containsExactly(RDAPValidationResult.builder()
            .code(errorCode)
            .value(value)
            .message(msg)
            .build());
    assertThat(results.getGroupErrorWarning()).containsExactly(validationName);
    assertThat(results.getGroupOk()).isEmpty();
  }

  protected void replaceValue(String jpath, Object value) {
    jsonObject = new JSONObject(JsonPath
        .parse(jsonObject.toString())
        .set(jpath, value)
        .jsonString());
  }

  protected void removeKey(String jpath) {
    jsonObject = new JSONObject(JsonPath
        .parse(jsonObject.toString())
        .delete(jpath)
        .jsonString());
  }
}
