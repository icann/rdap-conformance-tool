package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class ContainsConstExceptionParser extends ExceptionParser {

  protected ContainsConstExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  @Override
  public boolean matches(ValidationException e) {
    return e.getViolatedSchema() instanceof ArraySchema && ((ArraySchema)e.getViolatedSchema()).getContainedItemSchema() instanceof ConstSchema;
  }

  @Override
  protected void doParse() {
    ConstSchema constSchema = (ConstSchema) ((ArraySchema)e.getViolatedSchema()).getContainedItemSchema();
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> (int)constSchema.getUnprocessedProperties().get("errorCode")))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The "+e.getPointerToViolation()+" data structure does not include " + constSchema.getPermittedValue() +
            ".")
        .build());
  }
}
