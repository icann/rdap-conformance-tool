package org.icann.rdapconformance.validator.validators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.models.common.NoticeAndRemark;
import org.icann.rdapconformance.validator.validators.field.FieldValidation;
import org.json.JSONObject;

public class JsonFieldValidator {

  private final Schema schema;
  private final Object object;
  private final RDAPValidatorContext context;
  private ValidationException validationException;
  private JSONObject jsonObject = null;
  private final HashSet<String> requiredProperties;

  public JsonFieldValidator(Schema schema, Object object, RDAPValidatorContext context) {
    this.schema = schema;
    this.requiredProperties = new HashSet<>(((ObjectSchema) schema).getRequiredProperties());
    this.object = object;
    this.context = context;
    try {
      jsonObject = new JSONObject(new ObjectMapper().writeValueAsString(object));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }

    try {
      schema.validate(jsonObject);
    } catch (ValidationException e) {
      this.validationException = e;
    }
  }

  public boolean validateField(FieldValidation validation) {
    List<RDAPValidationResult> results = new ArrayList<>();

    String fieldName = validation.getName();
    if (validationException == null || isFieldOptionalAndNull(fieldName)) {
      return true;
    }

    for (String errorMsg : new HashSet<>(validationException.getAllMessages())) {
      if (!errorMsg.contains("#/" + fieldName)) {
        continue;
      }
      validation.validate(errorMsg, results, jsonObject);
    }

    for (RDAPValidationResult result : results) {
      context.addResult(result);
    }

    return results.isEmpty();
  }

  boolean isFieldOptionalAndNull(String name) {
    return !requiredProperties.contains(name) && jsonObject.get(name).equals(null);
  }
}
