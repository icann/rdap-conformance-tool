package org.icann.rdapconformance.validator.customvalidator;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;

public class LinkRelationsValidator extends DatasetValidator {

  public LinkRelationsValidator(LinkRelations linkRelations) {
    super(linkRelations, "linkRelations");
  }

  @Override
  protected String getErrorMsg() {
    return "The JSON value is not included as a Relation Name in linkRelations.";
  }
}
