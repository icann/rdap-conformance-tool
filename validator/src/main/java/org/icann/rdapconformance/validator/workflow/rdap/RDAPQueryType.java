package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.Set;

public enum RDAPQueryType {
  DOMAIN,
  HELP,
  NAMESERVER,
  ENTITY,
  NAMESERVERS,
  ERROR;

  public boolean isLookupQuery() {
    return Set.of(RDAPQueryType.DOMAIN, RDAPQueryType.NAMESERVER, RDAPQueryType.ENTITY)
        .contains(this);
  }
}
