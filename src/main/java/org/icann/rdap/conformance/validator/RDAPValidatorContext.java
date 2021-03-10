package org.icann.rdap.conformance.validator;

import java.util.HashMap;
import java.util.Map;
import org.icann.rdap.conformance.validator.configuration.ConfigurationFile;
import org.icann.rdap.conformance.validator.validators.StdRdapDomainLookupValidation;
import org.icann.rdap.conformance.validator.validators.StdRdapLdhNameValidation;
import org.icann.rdap.conformance.validator.validators.Validator;

/**
 * Service locator for RDAP validation.
 */
public class RDAPValidatorContext {

  private final Map<String, Validator> validators;
  private final ConfigurationFile configurationFile;
  private final RDAPDeserializer deserializer;

  public RDAPValidatorContext(ConfigurationFile configurationFile) {
    this.configurationFile = configurationFile;
    this.validators = new HashMap<>();
    this.validators.put("stdRdapDomainLookupValidation", new StdRdapDomainLookupValidation(this));
    this.validators.put("stdRdapLdhNameValidation", new StdRdapLdhNameValidation(this));
    this.deserializer = new RDAPDeserializer(this);
  }

  public RDAPDeserializer getDeserializer() {
    return deserializer;
  }

  public Validator getValidator(String name) {
    return this.validators.get(name);
  }

  public boolean isTestEnabled(String testCode) {
    // TODO
    return true;
  }

}
