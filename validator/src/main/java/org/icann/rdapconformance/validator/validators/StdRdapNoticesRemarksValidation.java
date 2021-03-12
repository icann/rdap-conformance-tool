package org.icann.rdapconformance.validator.validators;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.models.common.NoticeAndRemark;

public class StdRdapNoticesRemarksValidation extends Validator<NoticeAndRemark> {

  private static final List<String> AUTHORIZED_KEYS = List.of("title", "type", "description",
      "links");

  public StdRdapNoticesRemarksValidation(
      RDAPValidatorContext context) {
    super(context, NoticeAndRemark.class);
  }

  @Override
  public List<String> getAuthorizedKeys() {
    return AUTHORIZED_KEYS;
  }

  @Override
  public int getInvalidJsonErrorCode() {
    return -10700;
  }

  @Override
  public int getInvalidKeysErrorCode() {
    return -10701;
  }

  @Override
  protected int getDuplicateKeyErrorCode() {
    return -10702;
  }
}
