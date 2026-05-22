package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.ConformanceError;

public interface RDAPQuery {

    ConformanceError getErrorStatus();

    void setErrorStatus(ConformanceError errorStatus);

    boolean run();

    boolean validateStructureByQueryType(RDAPQueryType queryType);

    boolean isErrorContent();

    void addErrorsToErrorRdapResponse();

    String getData();

    Object getRawResponse();
}
