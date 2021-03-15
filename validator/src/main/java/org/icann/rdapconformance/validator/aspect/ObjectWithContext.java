package org.icann.rdapconformance.validator.aspect;

import org.icann.rdapconformance.validator.RDAPValidatorContext;

public interface ObjectWithContext {

    RDAPValidatorContext getContext();
}
