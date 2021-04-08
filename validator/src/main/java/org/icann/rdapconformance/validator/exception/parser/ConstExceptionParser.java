package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorResults;
import org.json.JSONObject;

public class ConstExceptionParser extends ExceptionParser {

  protected ConstExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  @Override
  public boolean matches(ValidationException e) {
    return e.getViolatedSchema() instanceof ConstSchema;
  }

  @Override
  protected void doParse() {
    ConstSchema constSchema = (ConstSchema) e.getViolatedSchema();
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> getErrorCodeFromViolatedSchema(e)))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The JSON value is not " + constSchema.getPermittedValue() + ".")
        .build());
  }
}
