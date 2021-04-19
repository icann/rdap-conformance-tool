package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public abstract class StringFormatExceptionParser<T> extends ExceptionParser {

  private final Class<T> formatValidator;

  protected StringFormatExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      Class<T> formatValidator) {
    super(e, schema, jsonObject, results);
    this.formatValidator = formatValidator;
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    return e.getViolatedSchema() instanceof StringSchema &&
        ((StringSchema) e.getViolatedSchema())
            .getFormatValidator().getClass().equals(formatValidator);
  }

  @Override
  protected void doParse() {
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(e.getMessage(e.getMessage()))
        .build());
  }
}
