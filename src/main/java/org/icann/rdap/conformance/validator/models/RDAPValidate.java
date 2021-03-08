package org.icann.rdap.conformance.validator.models;

import java.util.List;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.configuration.ConfigurationFile;

public interface RDAPValidate {

    List<RDAPValidationResult> validate(ConfigurationFile config);
}
