package org.icann.rdapconformance.validator.customvalidator;

import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;

public class LinkRelationsValidatorTest extends DatasetValidatorTest<LinkRelationsValidator> {

  public LinkRelationsValidatorTest() {
    super("linkRelations", new LinkRelationsValidator(mock(LinkRelations.class)));
  }
}