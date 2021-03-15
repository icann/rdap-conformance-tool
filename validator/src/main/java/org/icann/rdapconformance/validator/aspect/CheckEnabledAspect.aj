package org.icann.rdapconformance.validator.aspect;

import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.icann.rdapconformance.validator.aspect.annotation.CheckEnabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class CheckEnabledAspect {

  private static final Logger logger = LoggerFactory.getLogger(CheckEnabledAspect.class);

  @Around("execution(@CheckEnabled boolean *(..)) && @annotation(checkEnabled) && target(org.icann.rdapconformance.validator.aspect.ObjectWithContext)")
  public boolean checkTestEnabledOnRdapObject(final ProceedingJoinPoint pjp,
      final CheckEnabled checkEnabled)
      throws Throwable {
    ObjectWithContext object = (ObjectWithContext) pjp.getThis();
    int code = getCode(pjp, checkEnabled);
    if (!object.getContext().isTestEnabled(code)) {
      logger.info("Test {}.{} with code {} is disabled", object.getClass().getSimpleName(),
          pjp.getSignature().getName(), code);
      return true;
    }
    return (boolean) pjp.proceed();
  }

  static int getCode(final JoinPoint jp, final CheckEnabled checkEnabled) {
    validateAnnotation(checkEnabled);
    if (checkEnabled.code() != 0) {
      return checkEnabled.code();
    } else if (!checkEnabled.codeGetter().isEmpty()) {
      try {
        return (int) jp.getThis().getClass().getMethod(checkEnabled.codeGetter())
            .invoke(jp.getThis());
      } catch (Exception e) {
        throw new RuntimeException(String.format("Invalid method %s for class %s",
            checkEnabled.codeGetter(), jp.getThis().getClass().getSimpleName()), e);
      }
    } else {
      MethodSignature signature = (MethodSignature) jp.getSignature();
      String[] paramNames = signature.getParameterNames();
      Object[] paramValues = jp.getArgs();
      for (int i = 0; i < paramNames.length; i++) {
        if (paramNames[i].equals(checkEnabled.codeParam())) {
          return (int) paramValues[i];
        }
      }
      throw new RuntimeException(String.format("Invalid parameter %s for method %s",
          checkEnabled.codeParam(), signature.getName()));
    }
  }

  static void validateAnnotation(CheckEnabled checkEnabled) {
    int nbrSet = 0;
    if (checkEnabled.code() != 0) {
      nbrSet += 1;
    }
    if (!checkEnabled.codeGetter().isEmpty()) {
      nbrSet += 1;
    }
    if (!checkEnabled.codeParam().isEmpty()) {
      nbrSet += 1;
    }

    if (nbrSet == 0) {
      throw new RuntimeException(
          "@CheckEnabled annotation needs code, codeGetter or codeParam to be set");
    }

    if (nbrSet > 1) {
      throw new RuntimeException(
          "@CheckEnabled annotation needs only one of code, codeGetter or codeParam to be set");
    }
  }

}
