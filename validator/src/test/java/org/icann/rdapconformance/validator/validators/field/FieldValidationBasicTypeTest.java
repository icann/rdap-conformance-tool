package org.icann.rdapconformance.validator.validators.field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

import java.util.regex.Matcher;
import org.icann.rdapconformance.validator.validators.JsonFieldValidator;
import org.testng.annotations.Test;

public class FieldValidationBasicTypeTest {
  @Test
  public void testBasicTypePattern() {
    Matcher matcher = FieldValidationBasicType.basicTypePattern
        .matcher("#/description: expected type: JSONArray, found: String");
    assertThat(matcher.find()).isTrue();
    assertThat(matcher.group(1)).isEqualTo("JSONArray");
    assertThat(matcher.group(2)).isEqualTo("String");
  }

}