package org.icann.rdapconformance.validator.customvalidator;

import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;

public class MediaTypesValidatorTest extends DatasetValidatorTest {

  public MediaTypesValidatorTest() {
    super("mediaTypes", new DatasetValidator(mock(MediaTypes.class), "mediaTypes"));
  }
}