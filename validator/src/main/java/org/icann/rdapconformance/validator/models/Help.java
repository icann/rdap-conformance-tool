package org.icann.rdapconformance.validator.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.icann.rdapconformance.validator.models.domain.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Help extends RDAPValidate {

  private static final Logger logger = LoggerFactory.getLogger(Domain.class);

  // rdapConformance -- an array of strings, each providing a hint as to the specifications used in
  //                    the construction of the response.  This data structure appears only in the
  //                    topmost JSON object of a response.
  @JsonProperty
  protected String rdapConformance;

  // notices -- Information about the service providing RDAP information and/or information
  //            about the entire response.
  @JsonProperty
  protected String notices;

  @Override
  public boolean validate() {
    boolean result;
    result = this.validateField("notices", notices, "stdRdapNoticesRemarksValidation", -12503);
    result &= this.validateField("rdapConformance", rdapConformance, "stdRdapConformanceValidation",
        -12505);
    return result;
  }
}
