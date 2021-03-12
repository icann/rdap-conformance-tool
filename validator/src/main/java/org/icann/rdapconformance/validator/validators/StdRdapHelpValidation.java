package org.icann.rdapconformance.validator.validators;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.models.Help;

public class StdRdapHelpValidation extends Validator<Help> {

  private static final List<String> AUTHORIZED_KEYS = List.of("notices", "rdapConformance");

  public StdRdapHelpValidation(RDAPValidatorContext context) {
    super(context, Help.class);
  }

  @Override
  public String getDefinitionId() {
    return "stdRdapHelpValidation";
  }

  @Override
  public List<String> getAuthorizedKeys() {
    return AUTHORIZED_KEYS;
  }

  @Override
  public int getInvalidJsonErrorCode() {
    return -12500;
  }

  @Override
  public int getInvalidKeysErrorCode() {
    return -12501;
  }

  @Override
  protected int getDuplicateKeyErrorCode() {
    return -12502;
  }
}
