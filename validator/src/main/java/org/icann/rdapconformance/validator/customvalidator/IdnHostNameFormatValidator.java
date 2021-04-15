package org.icann.rdapconformance.validator.customvalidator;

import com.ibm.icu.text.IDNA;
import com.ibm.icu.text.IDNA.Error;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.everit.json.schema.FormatValidator;

public class IdnHostNameFormatValidator implements FormatValidator {

  private static final IDNA idna = IDNA.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII
      | IDNA.NONTRANSITIONAL_TO_UNICODE
      | IDNA.CHECK_BIDI
      | IDNA.CHECK_CONTEXTJ
      | IDNA.CHECK_CONTEXTO
      | IDNA.USE_STD3_RULES);

  @Override
  public Optional<String> validate(final String domain) {
    String[] labels = domain.split("\\.");
    Set<String> errors = new HashSet<>();
    validateDomain(domain, errors);

    if (labels.length < 2) {
      errors.add("LESS_THAN_TWO_LABELS");
    }

    for (String subdomain : labels) {
      validateDomain(subdomain, errors);
    }
    if (errors.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(errors.toString());
  }

  private void validateDomain(String label, Set<String> errors) {
    StringBuilder asciiDomain = new StringBuilder();
    IDNA.Info info = new IDNA.Info();
    idna.nameToASCII(label, asciiDomain, info);
    if (info.hasErrors()) {
      for (Error error : info.getErrors()) {
        errors.add(error.toString());
      }
    }
  }

  @Override
  public String formatName() {
    return "idn-hostname";
  }
}
