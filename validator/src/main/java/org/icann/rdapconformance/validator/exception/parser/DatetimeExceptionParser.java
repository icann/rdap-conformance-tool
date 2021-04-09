package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.json.JSONObject;

public class DatetimeExceptionParser extends ExceptionParser {

  protected DatetimeExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorContext context) {
    super(e, schema, jsonObject, context);
  }

  @Override
  public boolean matches(ValidationException e) {
    return e.getViolatedSchema() instanceof StringSchema &&
        ((StringSchema) e.getViolatedSchema())
            .getFormatValidator() instanceof DateTimeFormatValidator;
  }

  @Override
  protected void doParse() {
    context.addResult(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> getErrorCodeFromViolatedSchema(e)))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(
            "The JSON value shall be a syntactically valid time and date according to RFC3339.")
        .build());
  }
}
