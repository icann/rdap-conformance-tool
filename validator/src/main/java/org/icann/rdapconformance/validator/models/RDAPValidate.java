package org.icann.rdapconformance.validator.models;

import com.fasterxml.jackson.annotation.JacksonInject;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.aspect.ObjectWithContext;
import org.icann.rdapconformance.validator.aspect.annotation.CheckEnabled;
import org.icann.rdapconformance.validator.validators.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPValidate implements ObjectWithContext {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidate.class);

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
}
