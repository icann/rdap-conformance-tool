package org.icann.rdap.conformance.validator.models.domain;

import java.util.List;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.models.common.RDAPObject;

public class Domain extends RDAPObject {

  // variants

  // nameservers -- an array of nameserver objects

  // secureDNS

  // publicIds

  // network -- represents the IP network for which a reverse DNS domain is referenced


  @Override
  public List<RDAPValidationResult> validate() {
    List<RDAPValidationResult> results = super.validate();
    return results;
  }
}
