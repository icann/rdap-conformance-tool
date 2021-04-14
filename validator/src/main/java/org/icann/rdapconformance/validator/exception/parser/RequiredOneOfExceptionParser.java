package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Pattern;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class RequiredOneOfExceptionParser extends ExceptionParser {

  static Pattern basicTypePattern = Pattern.compile("expected type: (.+), found: (.+)");

  protected RequiredOneOfExceptionParser(ValidationExceptionNode e,
      Schema schema,
      JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
  }

  @Override
  public boolean matches(ValidationExceptionNode e) {
    return e.getKeyword() != null &&
        e.getKeyword().equals("oneOf") &&
        e.getViolatedSchema() instanceof CombinedSchema;
  }

  @Override
  protected void doParse() {

  }
}
