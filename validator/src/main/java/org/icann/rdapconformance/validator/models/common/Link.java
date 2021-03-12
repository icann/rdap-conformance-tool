package org.icann.rdapconformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidationResult;

public class Link extends Lang {

  @JsonProperty
  private String value;

  @JsonProperty
  private String rel;

  @JsonProperty
  private String href;

  @JsonProperty
  private String hreflang;

  @JsonProperty
  private String title;

  @JsonProperty
  private String media;

  @JsonProperty
  private String type;

  @Override
  public List<RDAPValidationResult> validate() {
    List<RDAPValidationResult> results = new ArrayList<>();
    return results;
  }
}
