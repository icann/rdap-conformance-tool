package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.json.JSONObject;

public class BasicTypeExceptionParser extends ExceptionParser {

  static Pattern basicTypePattern = Pattern.compile("expected type: (.+), found: (.+)");
  protected Matcher matcher;

  protected BasicTypeExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject, RDAPValidatorContext context) {
    super(e, schema, jsonObject, context);
    matcher = basicTypePattern.matcher(e.getMessage());
    matcher.find();
  }

  protected boolean matches(ValidationException e) {
    matcher = basicTypePattern.matcher(e.getMessage());
    return getValidationName(e) == null &&  matcher.find();
  }

  @Override
  public void doParse() {
    Object element = jsonObject.query(e.getPointerToViolation());
    String icannErrorMsg =
        "The JSON value is not a " + matcher.group(1).toLowerCase()
            + ".";
    String value = e.getPointerToViolation() + ":" + element.toString();
    if (matcher.group(1).equals("JSONArray")) {
      icannErrorMsg =
          "The " + e.getPointerToViolation() + " structure is not syntactically valid.";
      value = jsonObject.query(e.getPointerToViolation()).toString();
    }

    context.addResult(RDAPValidationResult.builder()
        .code(getErrorCodeFromViolatedSchema(e))
        .value(value)
        .message(icannErrorMsg)
        .build());
  }

  protected static String getValidationName(ValidationException e) {
    return (String) getPropertyFromViolatedSchema(e, "validationName");
  }
}
