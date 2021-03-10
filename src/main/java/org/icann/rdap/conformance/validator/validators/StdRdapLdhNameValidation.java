package org.icann.rdap.conformance.validator.validators;

import java.util.List;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.RDAPValidatorContext;

public class StdRdapLdhNameValidation extends Validator {


  public StdRdapLdhNameValidation(RDAPValidatorContext context) {
    super(context);
  }

  @Override
  public List<RDAPValidationResult> validate(String rdapContent) {
    return null;
  }
}
