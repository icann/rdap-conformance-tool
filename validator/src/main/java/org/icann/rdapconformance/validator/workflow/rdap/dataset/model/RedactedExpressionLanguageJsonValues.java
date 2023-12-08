package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;

public class RedactedExpressionLanguageJsonValues extends RDAPSubJsonValues {

  public RedactedExpressionLanguageJsonValues(RDAPJsonValues rdapJsonValues) {
    super(rdapJsonValues, JsonValueType.REDACTED_EXPRESSION_LANGUAGE);
  }
}
