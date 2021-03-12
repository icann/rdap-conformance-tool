package org.icann.rdapconformance.validator.models;

import com.fasterxml.jackson.annotation.JacksonInject;
import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.validators.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPValidate {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidate.class);

  @JacksonInject(value = "context")
  protected RDAPValidatorContext context;

  public abstract List<RDAPValidationResult> validate();

  protected void validateField(String fieldName, String fieldValue,
      String validationName,
      int errorCode,
      List<RDAPValidationResult> results) {
    Validator validator = this.context.getValidator(
        validationName);
    List<RDAPValidationResult> rdapConformanceResults = validator.validate(fieldValue);

    if (rdapConformanceResults.size() > 0) {
      results.addAll(rdapConformanceResults);
      logger.error("rdapConformance validation failed");
      results.add(RDAPValidationResult.builder()
          .code(errorCode)
          .value(fieldName + "/" + fieldValue)
          .message(
              "The value for the JSON name value does not pass " + fieldName + " validation "
                  + validationName +
                  ".")
          .build());
    }
    logger.debug("{}: OK", fieldName);
  }
}
