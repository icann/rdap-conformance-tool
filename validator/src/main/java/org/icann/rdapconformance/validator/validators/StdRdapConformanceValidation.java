package org.icann.rdapconformance.validator.validators;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.models.common.RDAPObject;

/**
 * TODO: inherit from a more basic validator
 */
public class StdRdapConformanceValidation extends Validator<RDAPObject> {

  private static final List<String> AUTHORIZED_KEYS = List.of("rdapConformance");

  public StdRdapConformanceValidation(
      RDAPValidatorContext context) {
    super(context, RDAPObject.class);
  }

  @Override
  public List<String> getAuthorizedKeys() {
    return AUTHORIZED_KEYS;
  }

  @Override
  public int getInvalidJsonErrorCode() {
    return -10500;
  }

  @Override
  public int getInvalidKeysErrorCode() {
    return -10501;
  }

  @Override
  public int getDuplicateKeyErrorCode() {
    return -10702;
  }
}
