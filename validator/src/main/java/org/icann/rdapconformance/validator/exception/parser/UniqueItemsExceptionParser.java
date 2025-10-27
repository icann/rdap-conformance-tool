package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class UniqueItemsExceptionParser extends ExceptionParser {

  protected UniqueItemsExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  protected UniqueItemsExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    return e.getKeyword() != null && e.getKeyword().equals("uniqueItems");
  }

  @Override
  protected void doParse() {
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(
            parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("duplicateItemsErrorCode")))
        .value(jsonObject.toString())
        .message("A " + e.getPointerToViolation() + " value appeared more than once.");

    if (queryContext != null) {
      results.add(builder.build(queryContext));
    } else {
      results.add(builder.build());
    }
  }
}
