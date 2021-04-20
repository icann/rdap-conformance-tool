package org.icann.rdapconformance.validator.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DefinitionError extends DefinitionAlert {

  @JsonCreator
  public DefinitionError(@JsonProperty(value = "code", required = true) int code,
      @JsonProperty(value = "notes", required = true) String notes) {
    super(code, notes);
  }
}
