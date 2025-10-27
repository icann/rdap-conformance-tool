package org.icann.rdapconformance.validator.exception.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.exception.ValidationExceptionNode;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

public class EnumExceptionParser extends ExceptionParser {

  static Pattern enumPattern = Pattern.compile("(.+) is not a valid enum value");
  private final Matcher matcher;

  protected EnumExceptionParser(ValidationExceptionNode e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorResults results) {
    super(e, schema, jsonObject, results);
    matcher = enumPattern.matcher(e.getMessage());
    matcher.find();
  }

  protected EnumExceptionParser(ValidationExceptionNode e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorResults results,
      org.icann.rdapconformance.validator.QueryContext queryContext) {
    super(e, schema, jsonObject, results, queryContext);
    matcher = enumPattern.matcher(e.getMessage());
    matcher.find();
  }

  public boolean matches(ValidationExceptionNode basicException) {
    return basicException.getViolatedSchema() instanceof EnumSchema;
  }

  @Override
  public void doParse() {
    EnumSchema enumSchema = (EnumSchema)e.getViolatedSchema();
    String schemaLocation = "";
    if (e.getSchemaLocation() != null) {
      schemaLocation = "Type=\"" + e.getSchemaLocation().replace("classpath://json-schema/", "") + "\"";
    }
    RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
        .code(parseErrorCode(e::getErrorCodeFromViolatedSchema))
        .value(e.getPointerToViolation() + ":" + jsonObject.query(e.getPointerToViolation())
            .toString())
        .message(e.getMessage("The JSON string is not included as a Value with " + schemaLocation
            + " dataset (" + enumSchema.getPossibleValuesAsList() + ")."));

    if (queryContext != null) {
      results.add(builder.build(queryContext));
    } else {
      results.add(builder.build());
    }
  }
}
