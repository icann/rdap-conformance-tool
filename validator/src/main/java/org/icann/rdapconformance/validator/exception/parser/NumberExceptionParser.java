package org.icann.rdapconformance.validator.exception.parser;

import java.text.MessageFormat;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class NumberExceptionParser extends ExceptionParser {

  protected NumberExceptionParser(ValidationExceptionNode e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    return e.getViolatedSchema() instanceof NumberSchema;
  }

  @Override
  protected void doParse() {
    NumberSchema numberSchema = (NumberSchema) e.getViolatedSchema();
    String type = "number";
    if (numberSchema.requiresInteger()) {
      type = "integer";
    }

    String icannErrorMsg = "The JSON value is not a " + type + ".";
    if (numberSchema.getMaximum() != null && numberSchema.getMinimum() != null) {
      icannErrorMsg = MessageFormat.format("The JSON value is not a {0} between {1} and {2}.",
          type, numberSchema.getMinimum(), numberSchema.getMaximum());
    }

    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(icannErrorMsg)
        .build());
  }
}
