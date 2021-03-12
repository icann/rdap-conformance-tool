package org.icann.rdapconformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.icann.rdapconformance.validator.RDAPValidationResult;

public class NoticeAndRemark extends Lang {

  // title -- string representing the title of the object
  @JsonProperty
  String title;

  // type -- string denoting a registered type of remark or notice
  // TODO enum?
  @JsonProperty
  String type;

  // description -- an array of strings for the purposes of conveying any descriptive text
  @JsonProperty
  List<String> description;

  // links --
  @JsonProperty
  List<Link> links;

  @Override
  public List<RDAPValidationResult> validate() {
    List<RDAPValidationResult> results = new ArrayList<>();
    return results;
  }
}
