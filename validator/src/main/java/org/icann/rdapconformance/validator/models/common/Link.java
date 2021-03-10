package org.icann.rdapconformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;

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

}
