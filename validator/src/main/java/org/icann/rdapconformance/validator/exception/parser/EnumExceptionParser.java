package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.json.JSONObject;

public class EnumExceptionParser extends ExceptionParser {

  static Pattern enumPattern = Pattern.compile("(.+) is not a valid enum value");
  private final Matcher matcher;

  protected EnumExceptionParser(ValidationException e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorContext context) {
    super(e, schema, jsonObject, context);
    matcher = enumPattern.matcher(e.getMessage());
    matcher.find();
  }

  protected boolean matches(ValidationException basicException) {
    return enumPattern.matcher(basicException.getMessage()).find();
  }

  @Override
  public void doParse() {
    context.addResult(RDAPValidationResult.builder()
        .code(getErrorCodeFromViolatedSchema(e))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()).toString())
        .message(
            "The JSON string is not included as a Value with Type=\"" + e.getSchemaLocation().replace("classpath://json-schema/", "")
                + "\" in the "
                + "RDAPJSONValues dataset.")
        .build());
  }
}
