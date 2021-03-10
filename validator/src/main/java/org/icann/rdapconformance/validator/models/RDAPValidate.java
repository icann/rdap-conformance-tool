package org.icann.rdapconformance.validator.models;

import com.fasterxml.jackson.annotation.JacksonInject;
import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;

public abstract class RDAPValidate {

  @JacksonInject(value="context")
  protected RDAPValidatorContext context;

  public abstract List<RDAPValidationResult> validate();
}
