package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.json.JSONObject;

public class MissingKeyExceptionParser extends ExceptionParser {

  static Pattern pattern = Pattern.compile("required key \\[(.+)\\] not found");
  protected Matcher matcher;

  protected MissingKeyExceptionParser(ValidationException e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorContext context) {
    super(e, schema, jsonObject, context);
    matcher = pattern.matcher(e.getMessage());
    matcher.find();
  }

  public boolean matches(ValidationException e) {
    matcher = pattern.matcher(e.getMessage());
    return matcher.find();
  }

  @Override
  public void doParse() {
    String key = matcher.group(1);
    context.addResult(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> (int) getPropertyFromViolatedSchema(e, key + "Missing")))
        .value(jsonObject.toString())
        .message("The " + key + " element does not exist.")
        .build());
  }
}
