package org.icann.rdapconformance.validator.validators;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.models.common.RDAPObject;

/**
 * TODO: inherit from a more basic validator
 */
public class StdRdapLdhNameValidation extends Validator<RDAPObject> {


  public StdRdapLdhNameValidation(RDAPValidatorContext context) {
    super(context, RDAPObject.class);
  }

  @Override
  public String getDefinitionId() {
    return null;
  }

  @Override
  public List<String> getAuthorizedKeys() {
    return null;
  }

  @Override
  public int getInvalidJsonErrorCode() {
    return 0;
  }

  @Override
  public int getInvalidKeysErrorCode() {
    return 0;
  }

  @Override
  public boolean validate(String rdapContent) {
    return true;
  }

  @Override
  protected int getDuplicateKeyErrorCode() {
    return 0;
  }
}
