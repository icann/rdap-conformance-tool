package org.icann.rdapconformance.validator.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CheckEnabled {

  int code() default 0;
  String codeGetter() default "";
  String codeParam() default "";
}
