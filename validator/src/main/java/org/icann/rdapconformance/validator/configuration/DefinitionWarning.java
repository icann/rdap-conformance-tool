package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefinitionWarning extends DefinitionAlert {

  @JsonCreator
  public DefinitionWarning(@JsonProperty(value = "code", required = true) int code,
      @JsonProperty(value = "notes") String notes) {
    super(code, notes);
  }
}
