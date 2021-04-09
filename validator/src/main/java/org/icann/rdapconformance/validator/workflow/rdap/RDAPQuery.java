package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.Optional;

public interface RDAPQuery {

  RDAPValidationStatus getErrorStatus();

  boolean run();

  Optional<Integer> getStatusCode();

  boolean checkWithQueryType(RDAPQueryType queryType);

  boolean isErrorContent();

  String getData();
}
