package org.icann.rdapconformance.validator.validators;


import org.icann.rdapconformance.validator.models.domain.Domain;

public class StdRdapDomainLookupValidationTest extends StdRdapValidationTest<Domain> {

  public StdRdapDomainLookupValidationTest() {
    super(Domain.class, "stdRdapDomainLookupValidation");
  }
}