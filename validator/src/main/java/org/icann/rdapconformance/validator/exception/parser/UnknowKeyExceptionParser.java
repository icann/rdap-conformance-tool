package org.icann.rdapconformance.validator.exception.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class UnknowKeyExceptionParser extends ExceptionParser {

  static Pattern unknownKeyPattern = Pattern.compile("extraneous key \\[(.+)\\] is not permitted");
  private Matcher matcher;

  public UnknowKeyExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject, RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  public boolean matches(ValidationExceptionNode e) {
    matcher = unknownKeyPattern.matcher(e.getMessage());
    return matcher.find();
  }

  @Override
  public void doParse() {
    String key = matcher.group(1);
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema("unknownKeys")))
        .value(e.getPointerToViolation() + "/" + key + ":" + (((JSONObject) jsonObject
            .query(e.getPointerToViolation())).get(key)))
        .message("The name in the name/value pair is not of: " + getAuthorizedProperties() + ".")
        .build());
  }

  private String getAuthorizedProperties() {
    List<String> authorizedProperties =
        new ArrayList<>(((ObjectSchema) e.getViolatedSchema()).getPropertySchemas().keySet());
    Collections.sort(authorizedProperties);
    return String.join(", ", authorizedProperties);
  }
}
