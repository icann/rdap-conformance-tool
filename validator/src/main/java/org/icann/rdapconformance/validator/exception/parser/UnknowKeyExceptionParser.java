package org.icann.rdapconformance.validator.exception.parser;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorResults;
import org.icann.rdapconformance.validator.schema.SchemaNode;
import org.json.JSONObject;

public class UnknowKeyExceptionParser extends ExceptionParser {

  static Pattern unknownKeyPattern = Pattern.compile("extraneous key \\[(.+)\\] is not permitted");
  private Matcher matcher;

  public UnknowKeyExceptionParser(ValidationException e, Schema schema,
      JSONObject jsonObject, RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  public boolean matches(ValidationException e) {
    matcher = unknownKeyPattern.matcher(e.getMessage());
    return matcher.find();
  }

  @Override
  public void doParse() {
    String key = matcher.group(1);
    SchemaNode schemaNode = SchemaNode.create(null, e.getViolatedSchema());
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> schemaNode.getErrorCode("unknownKeys")))
        .value(e.getPointerToViolation() + "/" + key + ":" + (((JSONObject) jsonObject
            .query(e.getPointerToViolation())).get(key)))
        .message("The name in the name/value pair is not of: " + getAuthorizedProperties() + ".")
        .build());
  }

  private String getAuthorizedProperties() {
    List<String> authorizedProperties =
        ((ObjectSchema) e.getViolatedSchema()).getPropertySchemas().keySet().stream().collect(Collectors.toList());
    Collections.sort(authorizedProperties);
    return String.join(", ", authorizedProperties);
  }
}
