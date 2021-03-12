package org.icann.rdapconformance.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.validators.StdRdapDomainLookupValidation;
import org.icann.rdapconformance.validator.validators.StdRdapHelpValidation;
import org.icann.rdapconformance.validator.validators.StdRdapLdhNameValidation;
import org.icann.rdapconformance.validator.validators.StdRdapNoticesRemarksValidation;
import org.icann.rdapconformance.validator.validators.Validator;

/**
 * Service locator for RDAP validation.
 */
public class RDAPValidatorContext {

  private final Map<String, Validator> validators;
  private final ConfigurationFile configurationFile;
  private final RDAPDeserializer deserializer;
  private final List<RDAPValidationResult> results = new ArrayList<>();

  public RDAPValidatorContext(ConfigurationFile configurationFile) {
    this.configurationFile = configurationFile;
    this.validators = new HashMap<>();
    this.validators.put("stdRdapDomainLookupValidation", new StdRdapDomainLookupValidation(this));
    this.validators.put("stdRdapLdhNameValidation", new StdRdapLdhNameValidation(this));
    this.validators.put("stdRdapHelpValidation", new StdRdapHelpValidation(this));
    this.validators.put("stdRdapNoticesRemarksValidation", new StdRdapNoticesRemarksValidation(this));
    this.deserializer = new RDAPDeserializer(this);
  }

  public RDAPDeserializer getDeserializer() {
    return deserializer;
  }

  public Validator getValidator(String name) {
    return this.validators.get(name);
  }

  public boolean isTestEnabled(int testCode) {
    return !configurationFile.getDefinitionIgnore().contains(testCode);
  }

  public void addResult(RDAPValidationResult result) {
    this.results.add(result);
  }

  public List<RDAPValidationResult> getResults() {
    return results;
  }
}
