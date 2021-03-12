package org.icann.rdapconformance.validator.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
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
}
