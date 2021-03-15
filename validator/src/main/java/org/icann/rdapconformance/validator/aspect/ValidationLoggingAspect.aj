package org.icann.rdapconformance.validator.aspect;

import static org.icann.rdapconformance.validator.aspect.CheckEnabledAspect.getCode;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.icann.rdapconformance.validator.aspect.annotation.CheckEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class ValidationLoggingAspect {

  private static final Logger logger = LoggerFactory.getLogger(ValidationLoggingAspect.class);

  @Pointcut("execution(* *.validate(..)) && target(org.icann.rdapconformance.validator.validators.Validator)")
  private void validatorValidate() {

  }

  @Pointcut("execution(* *.validate(..)) && target(org.icann.rdapconformance.validator.models.RDAPValidate)")
  private void rdapObjectValidate() {

  }

  @Pointcut("validatorValidate() || rdapObjectValidate()")
  private void validate() {

  }

  @Pointcut("execution(* *.validateField(..)) && target(org.icann.rdapconformance.validator.models.RDAPValidate) && args(fieldName)")
  private void fieldValidate(String fieldName) {

  }


  @Before("validate()")
  public void LogBeforeValidate(final JoinPoint jp) {
    String value = jp.getThis().getClass().getSimpleName();
    logger.debug("Starting {} validation", value);
  }

  @AfterReturning(pointcut = "validate()", returning = "result")
  public void LogAfterValidate(final JoinPoint jp, final boolean result) {
    String value = jp.getThis().getClass().getSimpleName();
    if (!result) {
      logger.info("{} validation failed", value);
    } else {
      logger.debug("{} validation OK", value);
    }
  }

  @Before("fieldValidate(fieldName)")
  public void LogBeforeFieldValidate(final String fieldName) {
    logger.debug("Starting field {} validation", fieldName);
  }

  @AfterReturning(pointcut = "fieldValidate(fieldName)", returning = "result")
  public void LogAfterMethod(final String fieldName, final boolean result) {
    if (!result) {
      logger.info("{} validation failed", fieldName);
    } else {
      logger.debug("{} validation OK", fieldName);
    }
  }


  @Before("execution(@CheckEnabled * *(..)) && @annotation(checkEnabled)")
  public void LogBeforeFieldValidate(final JoinPoint jp, final CheckEnabled checkEnabled) {
    int code = getCode(jp, checkEnabled);
    logger.debug("Starting validation for code {}", code);
  }

  @AfterReturning(pointcut = "execution(@CheckEnabled * *(..)) && @annotation(checkEnabled)",
      returning = "result")
  public void LogAfterMethod(final JoinPoint jp, final CheckEnabled checkEnabled,
      final boolean result) {
    int code = getCode(jp, checkEnabled);
    if (!result) {
      logger.info("Validation for code {} failed", code);
    } else {
      logger.debug("Validation for code {} OK", code);
    }
  }

}
