package org.icann.rdap.conformance.validator.validators;

import java.util.List;
import org.icann.rdap.conformance.validator.RDAPValidationResult;

public interface Validator {

  List<RDAPValidationResult> validate();
}
