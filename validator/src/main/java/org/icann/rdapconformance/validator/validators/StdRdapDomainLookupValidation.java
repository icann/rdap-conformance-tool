package org.icann.rdapconformance.validator.validators;

import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidatorContext;
import org.icann.rdapconformance.validator.models.domain.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdRdapDomainLookupValidation extends Validator<Domain> {

  private static final Logger logger = LoggerFactory.getLogger(StdRdapDomainLookupValidation.class);
  private static final List<String> AUTHORIZED_KEYS = List.of("objectClassName", "handle",
      "ldhName", "unicodeName", "variants", "nameservers", "secureDNS", "entities", "status",
      "publicIds", "remarks", "links", "port43", "events", "notices", "rdapConformance");

  public StdRdapDomainLookupValidation(RDAPValidatorContext context) {
    super(context, Domain.class);
  }

  @Override
  public String getDefinitionId() {
    return "stdRdapDomainLookupValidation";
  }

  @Override
  public List<String> getAuthorizedKeys() {
    return AUTHORIZED_KEYS;
  }

  @Override
  public int getInvalidJsonErrorCode() {
    return -12200;
  }

  @Override
  public int getInvalidKeysErrorCode() {
    return -12201;
  }

  @Override
  protected int getDuplicateKeyErrorCode() {
    return -12202;
  }
}
