package org.icann.rdapconformance.validator.models.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.icann.rdapconformance.validator.RDAPValidationResult;
import org.icann.rdapconformance.validator.models.common.RDAPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Domain extends RDAPObject {
  // TODO seems that there should not be lang in Domain

  private static final Logger logger = LoggerFactory.getLogger(Domain.class);

  // ldhName -- a string describing an object name in LDH form
  @JsonProperty
  String ldhName;

  // unicodeName -- a string containing an object name with U-labels
  @JsonProperty
  String unicodeName;

  // variants
  @JsonProperty
  Object variants;

  // nameservers -- an array of nameserver objects
  @JsonProperty
  Object nameservers;

  // secureDNS
  @JsonProperty
  Object secureDNS;

  // publicIds
  @JsonProperty
  Object publicIds;

  // network -- represents the IP network for which a reverse DNS domain is referenced
  // Not used here.
  // Object network;


  @Override
  public boolean validate() {
    boolean result = true;
    // 4. For the JSON name objectClassName, the value shall be "domain".
    if (!this.objectClassName.equals("domain")) {
      logger.error("Invalid objectClassName {}", this.objectClassName);
      this.context.addResult(RDAPValidationResult.builder()
          .code(-12203)
          .value("objectClassName/" + this.objectClassName)
          .message("The JSON value is not \"domain\".")
          .build());
      result = false;
    }
    // 5. If the JSON name handle exists, the value shall be a JSON string data type.
    if (null != this.handle) {
      try {
        String handleStr = (String) this.handle;
      } catch (ClassCastException e) {
        logger.error("Invalid handle {}", this.handle.toString());
        this.context.addResult(RDAPValidationResult.builder()
            .code(-12204)
            .value("handle/" + this.handle.toString())
            .message("The JSON value is not a string.")
            .build());
        result = false;
      }
    }
    // 6. If the JSON name ldhName exists, the value shall pass the test LDH name
    // [stdRdapLdhNameValidation].
    if (null != this.ldhName) {
      if (!this.validateField("ldhName", ldhName, "stdRdapLdhNameValidation", -12205)) {
        result = false;
      }
    }

    return result;
  }
}
