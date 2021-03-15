package org.icann.rdapconformance.validator.models;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.aspect.ObjectWithContext;
import org.icann.rdapconformance.validator.aspect.annotation.CheckEnabled;
import org.icann.rdapconformance.validator.validators.Validator;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPValidate implements ObjectWithContext {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidate.class);

  @JsonIgnore
  @JacksonInject(value = "context")
  protected RDAPValidatorContext context;

  public abstract boolean validate();

  @CheckEnabled(codeParam = "errorCode")
  protected boolean validateField(String fieldName, String fieldValue, String validationName,
      int errorCode) {
    if (fieldValue == null) {
      return true;
    }
    Validator<?> validator = this.context.getValidator(validationName);

    if (!validator.validate(fieldValue)) {
      this.context.addResult(RDAPValidationResult.builder()
          .code(errorCode)
          .value(fieldName + "/" + fieldValue)
          .message(
              String.format("The value for the JSON name value does not pass %s validation [%s].",
                  fieldName, validationName))
          .build());
      return false;
    }
    return true;
  }

  public RDAPValidatorContext getContext() {
    return context;
  }

  protected Schema getSchema(String name) {
    JSONObject jsonSchema = new JSONObject(
        new JSONTokener(
            Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("json-schema/" + name))));
    SchemaLoader schemaLoader = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .schemaJson(jsonSchema)
        .resolutionScope("classpath://json-schema/")
        .draftV7Support()
        .build();
    return schemaLoader.load().build();
  }
}
