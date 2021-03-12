package org.icann.rdapconformance.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.models.RDAPValidate;
import org.icann.rdapconformance.validator.validators.StdRdapDomainLookupValidation;
import org.icann.rdapconformance.validator.validators.StdRdapHelpValidation;
import org.icann.rdapconformance.validator.validators.StdRdapLdhNameValidation;
import org.icann.rdapconformance.validator.validators.StdRdapNoticesRemarksValidation;
import org.icann.rdapconformance.validator.validators.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service locator for RDAP validation.
 */
public class RDAPValidatorContext {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidatorContext.class);

  private final Map<String, Validator> validators = new HashMap<>();
  private final Map<String, Class<? extends Validator>> validatorClasses = new HashMap<>();
  private final ConfigurationFile configurationFile;
  private final RDAPDeserializer deserializer;
  private final List<RDAPValidationResult> results = new ArrayList<>();

  public RDAPValidatorContext(ConfigurationFile configurationFile) {
    this.configurationFile = configurationFile;
    this.validatorClasses.put("stdRdapDomainLookupValidation", StdRdapDomainLookupValidation.class);
    this.validatorClasses.put("stdRdapLdhNameValidation", StdRdapLdhNameValidation.class);
    this.validatorClasses.put("stdRdapHelpValidation", StdRdapHelpValidation.class);
    this.validatorClasses
        .put("stdRdapNoticesRemarksValidation", StdRdapNoticesRemarksValidation.class);
    this.deserializer = new RDAPDeserializer(this);
  }

  public RDAPDeserializer getDeserializer() {
    return deserializer;
  }

  public Validator<? extends RDAPValidate> getValidator(String name) {
    if (!this.validators.containsKey(name)) {
      Class<? extends Validator> validatorClass = this.validatorClasses.get(name);
      try {
        Validator validator = validatorClass.getDeclaredConstructor(RDAPValidatorContext.class)
            .newInstance(this);
        this.validators.put(name, validator);
      } catch (Exception e) {
        logger.error("Cannot create validator", e);
      }
    }
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
