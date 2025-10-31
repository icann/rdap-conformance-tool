package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class ComplexTypeExceptionParser extends ExceptionParser {

  static Pattern typePattern = Pattern.compile("expected type: (.+), found: (.+)");
  protected Matcher matcher;
  private String basicType;

  protected ComplexTypeExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  protected ComplexTypeExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results, org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    matcher = typePattern.matcher(e.getMessage());
    if (matcher.find()) {
      basicType = matcher.group(1);
      return basicType.equals("JSONArray") || basicType.equals("JSONObject");
    }
    return false;
  }

  @Override
  protected void doParse() {
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(parseErrorCode(() -> (int)e.getPropertyFromViolatedSchema("structureInvalid")))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message("The " + e.getPointerToViolation() + " structure is not syntactically valid.");

    results.add(builder.build(queryContext));
  }
}
