package org.icann.rdapconformance.validator.validators;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.RDAPValidatorContext;

public class StdRdapLdhNameValidation extends Validator {


  public StdRdapLdhNameValidation(RDAPValidatorContext context) {
    super(context);
  }

  @Override
  public List<RDAPValidationResult> validate(String rdapContent) {
    return null;
  }
}
