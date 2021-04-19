package org.icann.rdapconformance.validator.customvalidator;

import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;

public class MediaTypesValidatorTest extends DatasetValidatorTest<MediaTypesValidator> {

  public MediaTypesValidatorTest() {
    super("mediaTypes", new MediaTypesValidator(mock(MediaTypes.class)));
  }
}