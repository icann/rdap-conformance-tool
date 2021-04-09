package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class EnumExceptionParser extends ExceptionParser {

  static Pattern enumPattern = Pattern.compile("(.+) is not a valid enum value");
  private final Matcher matcher;

  protected EnumExceptionParser(ValidationException e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
    matcher = enumPattern.matcher(e.getMessage());
    matcher.find();
  }

  public boolean matches(ValidationException basicException) {
    return basicException.getViolatedSchema() instanceof EnumSchema;
  }

  @Override
  public void doParse() {
    EnumSchema enumSchema = (EnumSchema)e.getViolatedSchema();
    results.add(RDAPValidationResult.builder()
        .code(parseErrorCode(() -> getErrorCodeFromViolatedSchema(e)))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation()).toString())
        .message(
            "The JSON string is not included as a Value with Type=\"" + e.getSchemaLocation().replace("classpath://json-schema/", "")
                + "\" dataset ("+enumSchema.getPossibleValuesAsList()+").")
        .build());
  }
}
