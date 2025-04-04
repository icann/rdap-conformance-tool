package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.Set;

public enum RDAPQueryType {
  DOMAIN,
  HELP,
  NAMESERVER,
  ENTITY,
  AUTNUM,
  IP_NETWORK,
  NAMESERVERS,
  ERROR;

  public boolean isLookupQuery() {
    return Set.of(RDAPQueryType.DOMAIN, RDAPQueryType.NAMESERVER, RDAPQueryType.ENTITY, RDAPQueryType.AUTNUM, RDAPQueryType.IP_NETWORK)
        .contains(this);
  }
}
