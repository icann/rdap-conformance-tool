package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class VcardExceptionParser extends ExceptionParser {

  protected VcardExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    return e.getPointerToViolation() != null && e.getPointerToViolation().contains("vcardArray");
  }

  @Override
  protected void doParse() {
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(-12305)
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(
            "The value for the JSON name value is not a syntactically valid vcardArray.");

    results.add(builder.build(queryContext));
  }
}
