package org.icann.rdapconformance.validator.exception.parser;

import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class DependenciesExceptionParser extends ExceptionParser {
  static Pattern pattern = Pattern.compile("property \\[(.+)\\] is required");
  protected Matcher matcher;

  protected DependenciesExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
    matcher = pattern.matcher(e.getMessage());
    matcher.find();
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    return e.getKeyword() != null &&
        e.getViolatedSchema() instanceof ObjectSchema &&
        e.getKeyword().equals("dependencies") &&
        pattern.matcher(e.getMessage()).find();
  }

  @Override
  protected void doParse() {
    String key = matcher.group(1);
    ObjectSchema objectSchema = (ObjectSchema) e.getViolatedSchema();
    String parentKey = "parent";
    for (Entry<String, Set<String>> entry : objectSchema.getPropertyDependencies().entrySet()) {
      if (entry.getValue().contains(key)) {
        parentKey = entry.getKey();
      }
    }
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> (int) e.getPropertyFromViolatedSchema(key + "Missing")))
        .value(jsonObject.query(e.getPointerToViolation()).toString())
        .message("A " + parentKey + " structure was found but an " + key + " was not.")
        .build());
  }
}
