package org.icann.rdap.conformance.validator.validators;

import java.util.List;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.RDAPValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Validator {

  private static final Logger logger = LoggerFactory.getLogger(Validator.class);

  protected final RDAPValidatorContext context;

  public Validator(RDAPValidatorContext context) {
    this.context = context;
  }

  /**
   * Perform validation of the RDAP content.
   */
  public abstract List<RDAPValidationResult> validate(String rdapContent);
}
