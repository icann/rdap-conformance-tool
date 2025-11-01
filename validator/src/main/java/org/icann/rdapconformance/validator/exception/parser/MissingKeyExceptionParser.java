package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class MissingKeyExceptionParser extends ExceptionParser {

  static Pattern pattern = Pattern.compile("required key \\[(.+)\\] not found");
  protected Matcher matcher;


  protected MissingKeyExceptionParser(ValidationExceptionNode e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
    matcher = pattern.matcher(e.getMessage());
    matcher.find();
  }

  public boolean matches(ValidationExceptionNode e) {
    matcher = pattern.matcher(e.getMessage());
    return matcher.find();
  }

  @Override
  public void doParse() {
    String key = matcher.group(1);
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema(key + "Missing")))
        .value(jsonObject.toString())
        .message("The " + key + " element does not exist.");

    results.add(builder.build(queryContext));
  }
}
