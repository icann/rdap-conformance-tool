package org.icann.rdapconformance.validator.workflow.rdap;

import java.util.Optional;
import org.icann.rdapconformance.validator.ConnectionStatus;

public interface RDAPQuery {

  ConnectionStatus getErrorStatus();

  boolean run();

  Optional<Integer> getStatusCode();

  boolean checkWithQueryType(RDAPQueryType queryType);

  boolean isErrorContent();

  String getData();

  Object getRawResponse();

  void setResults(RDAPValidatorResults results);
}
