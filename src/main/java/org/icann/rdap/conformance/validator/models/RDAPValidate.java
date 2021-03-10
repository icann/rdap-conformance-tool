package org.icann.rdap.conformance.validator.models;

import com.fasterxml.jackson.annotation.JacksonInject;
import java.util.List;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.RDAPValidatorContext;

public abstract class RDAPValidate {

  @JacksonInject(value="context")
  protected RDAPValidatorContext context;

  public abstract List<RDAPValidationResult> validate();
}
