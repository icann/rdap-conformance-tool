package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class ConstExceptionParser extends ExceptionParser {

  protected ConstExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  protected ConstExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results, org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    return e.getViolatedSchema() instanceof ConstSchema;
  }

  @Override
  protected void doParse() {
    ConstSchema constSchema = (ConstSchema) e.getViolatedSchema();
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The JSON value is not " + constSchema.getPermittedValue() + ".");

    results.add(builder.build(queryContext));
  }
}
