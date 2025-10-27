package org.icann.rdapconformance.validator.exception.parser;

import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.internal.DateTimeFormatValidator;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class DatetimeExceptionParser extends StringFormatExceptionParser<DateTimeFormatValidator> {

  protected DatetimeExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results, DateTimeFormatValidator.class);
  }

  protected DatetimeExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, DateTimeFormatValidator.class, queryContext);
  }

  @Override
  protected void doParse() {
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(
            "The JSON value shall be a syntactically valid time and date according to RFC3339.");

    if (queryContext != null) {
      results.add(builder.build(queryContext));
    } else {
      results.add(builder.build());
    }
  }
}
