package org.icann.rdapconformance.validator.customvalidator;

import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;

public class LinkRelationsValidator implements FormatValidator {

  private final LinkRelations linkRelations;

  public LinkRelationsValidator(LinkRelations linkRelations) {
    this.linkRelations = linkRelations;
  }

  public LinkRelations getLinkRelations() {
    return linkRelations;
  }

  @Override
  public Optional<String> validate(String s) {
    if (linkRelations.isInvalid(s)) {
      return Optional.of("The JSON value is not included as a Relation Name in linkRelations.");
    }
    return Optional.empty();
  }

  @Override
  public String formatName() {
    return "linkRelations";
  }
}
