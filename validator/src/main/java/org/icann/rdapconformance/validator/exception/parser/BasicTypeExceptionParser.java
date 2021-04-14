package org.icann.rdapconformance.validator.exception.parser;

import java.text.MessageFormat;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class BasicTypeExceptionParser extends ExceptionParser {

  static Pattern basicTypePattern = Pattern.compile("expected type: (.+), found: (.+)");
  private String basicType;
  protected Matcher matcher;
  private Set<?> basicTypes = Set.of(
      BooleanSchema.class,
      StringSchema.class,
      NullSchema.class
      );

  protected BasicTypeExceptionParser(ValidationExceptionNode e, Schema schema,
      JSONObject jsonObject, RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
    matcher = basicTypePattern.matcher(e.getMessage());
    if (matcher.find()) {
      basicType = matcher.group(1);
    }
  }

  public boolean matches(ValidationExceptionNode e) {
    return basicTypePattern.matcher(e.getMessage()).find() && basicTypes.contains(e.getViolatedSchema().getClass());
  }

  @Override
  public void doParse() {
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()))
        .message(e.getMessage("The JSON value is not a " + basicType.toLowerCase() + "."))
        .build());
  }
}
