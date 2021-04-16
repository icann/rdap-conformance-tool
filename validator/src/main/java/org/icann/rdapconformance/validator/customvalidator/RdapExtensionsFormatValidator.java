package org.icann.rdapconformance.validator.customvalidator;

import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;

public class RdapExtensionsFormatValidator implements FormatValidator {

  private final RDAPExtensions rdapExtensions;

  public RdapExtensionsFormatValidator(RDAPExtensions rdapExtensions) {
    this.rdapExtensions = rdapExtensions;
  }

  @Override
  public Optional<String> validate(String subject) {
    if (rdapExtensions.isInvalid(subject)) {
      return Optional.of("The JSON string is not included as an Extension Identifier in "
          + "RDAPExtensions.");
    }
    return Optional.empty();
  }

  @Override
  public String formatName() {
    return "rdapExtensions";
  }
}
