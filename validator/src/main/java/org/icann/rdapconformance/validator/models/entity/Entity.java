package org.icann.rdapconformance.validator.models.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.icann.rdapconformance.validator.models.common.RDAPObject;

public class Entity extends RDAPObject {

  // roles -- an array of strings, each signifying the relationship an object would have with its
  //          closest containing object
  @JsonProperty
  // TODO role enum ?
  private List<String> roles;

  // TBC
}
