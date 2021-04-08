package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Pattern;
import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.json.JSONObject;

public class RegexExceptionParser extends ExceptionParser {

  static Pattern regexPattern = Pattern.compile("string (.+) does not match pattern (.+)");

  protected RegexExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorContext context) {
    super(e, schema, jsonObject, context);
  }

  @Override
  public boolean matches(ValidationException e) {
    if (e.getViolatedSchema() instanceof StringSchema) {
      return ((StringSchema) e.getViolatedSchema()).getPattern() != null
          && regexPattern.matcher(e.getMessage()).find();
    }
    return false;
  }

  @Override
  protected void doParse() {
    StringSchema stringSchema = (StringSchema) e.getViolatedSchema();
    context.addResult(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> getErrorCodeFromViolatedSchema(e)))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The value of the JSON string data in the "+e.getPointerToViolation()+" does not conform to "
            + e.getSchemaLocation().replace("classpath://json-schema/", "") + " syntax.")
        .build());
  }
}
