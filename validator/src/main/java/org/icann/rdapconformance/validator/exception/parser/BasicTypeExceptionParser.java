package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class BasicTypeExceptionParser extends ExceptionParser {

  static Pattern basicTypePattern = Pattern.compile("expected type: (.+), found: (.+)");
  protected Matcher matcher;

  protected BasicTypeExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject, RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
    matcher = basicTypePattern.matcher(e.getMessage());
    matcher.find();
  }

  public boolean matches(ValidationException e) {
    return basicTypePattern.matcher(e.getMessage()).find();
  }

  @Override
  public void doParse() {
    Object element = jsonObject.query(e.getPointerToViolation());
    String icannErrorMsg =
        "The JSON value is not a " + matcher.group(1).toLowerCase() + ".";
    String value = e.getPointerToViolation() + ":" + element.toString();
    int errorCode = parseErrorCode(() -> getErrorCodeFromViolatedSchema(e));
    if (matcher.group(1).equals("JSONArray")) {
      icannErrorMsg =
          "The " + e.getPointerToViolation() + " structure is not syntactically valid.";
      value =
          e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()).toString();
      errorCode = parseErrorCode(() ->
          (int) e.getViolatedSchema().getUnprocessedProperties().get("structureInvalid"));
    }

    results.add(RDAPValidationResult.builder()
        .code(errorCode)
        .value(value)
        .message(icannErrorMsg)
        .build());
  }

  protected String getValidationName(ValidationException e) {
    return (String) getPropertyFromViolatedSchema(e, "validationName");
  }
}
