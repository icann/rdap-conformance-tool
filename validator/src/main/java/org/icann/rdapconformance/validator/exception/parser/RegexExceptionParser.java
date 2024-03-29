package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class RegexExceptionParser extends ExceptionParser {

  static Pattern regexPattern = Pattern.compile("string (.+) does not match pattern (.+)");

  protected RegexExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    if (e.getViolatedSchema() instanceof StringSchema) {
      return ((StringSchema) e.getViolatedSchema()).getPattern() != null
          && regexPattern.matcher(e.getMessage()).find();
    }
    return false;
  }

  @Override
  protected void doParse() {
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(e.getMessage("The value of the JSON string data in the " + e.getPointerToViolation()
            + " does not conform to "
            + e.getSchemaLocation() + " syntax."))
        .build());
  }
}
