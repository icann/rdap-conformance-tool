package org.icann.rdapconformance.validator.customvalidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.testng.annotations.Test;

public class LinkRelationsValidatorTest extends CustomValidatorTest<LinkRelationsValidator> {

  public LinkRelationsValidatorTest() {
    super("linkRelations", new LinkRelationsValidator(mock(LinkRelations.class)));
  }

  @Test
  public void valid() {
    doReturn(false).when(formatValidator.getLinkRelations()).isInvalid(any());
    assertThat(formatValidator.validate("a string")).isEmpty();
  }

  @Test
  public void invalid() {
    doReturn(true).when(formatValidator.getLinkRelations()).isInvalid(any());
    assertThat(formatValidator.validate("a string"))
        .contains("The JSON value is not included as a Relation Name in linkRelations.");
  }
}