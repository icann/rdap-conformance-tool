package org.icann.rdapconformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.icann.rdapconformance.validator.models.RDAPValidate;

public abstract class Lang extends RDAPValidate {

  // lang -- This data structure consists solely of a name/value pair, where the name is "lang"
  //         and the value is a string containing a language identifier as described in [RFC5646]
  @JsonProperty
  private String lang;
}
