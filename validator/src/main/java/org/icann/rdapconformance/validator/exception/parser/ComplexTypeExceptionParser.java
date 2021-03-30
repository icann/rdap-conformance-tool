package org.icann.rdapconformance.validator.exception.parser;

import java.text.MessageFormat;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.json.JSONObject;

public class ComplexTypeExceptionParser extends BasicTypeExceptionParser {

  protected ComplexTypeExceptionParser(ValidationException e,
      Schema schema, JSONObject jsonObject,
      RDAPValidatorContext context) {
    super(e, schema, jsonObject, context);
  }

  /**
   * A complex type has the same error structure as a basic type, but has a subvalidation process.
   */
  protected boolean matches(ValidationException e) {
    return getValidationName(e) != null && basicTypePattern.matcher(e.getMessage()).find();
  }

  @Override
  public void doParse() {
    String key = e.getPointerToViolation();
    Object element = jsonObject.query(key);

    String validationName = getValidationName(e);
    context.addResult(RDAPValidationResult.builder()
        .code(getErrorCode(validationName))
        .value(key + ":" + element)
        .message(MessageFormat.format("The value for the JSON name value does not pass {0} "
            + "validation [{1}].", key, validationName))
        .build());
  }
}
