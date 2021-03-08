package org.icann.rdap.conformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.configuration.ConfigurationFile;
import org.icann.rdap.conformance.validator.models.RDAPValidate;
import org.icann.rdap.conformance.validator.models.entity.Entity;

public abstract class RDAPObject extends Lang implements RDAPValidate {

  // handle -- a string representing a registry unique identifier of the object
  @JsonProperty
  private String handle;

  // status -- an array of strings indicating the state of the IP network
  @JsonProperty
  // TODO List enum?
  private List<String> status;

  // entities -- an array of entity objects
  @JsonProperty
  private List<Entity> entities;

  // remarks -- see NoticeAndRemark
  @JsonProperty
  // TODO List enum?
  private List<String> remarks;

  // links -- signify links to other resources on the Internet.
  @JsonProperty
  private List<Link> links;

  // port43 -- a simple string containing the fully qualified host name or IP address of the WHOIS server where the
  //           containing object instance may be found.
  @JsonProperty
  private String port43;

  // events -- represents events that have occurred on an instance of an object class.
  @JsonProperty
  private List<Event> events;

  @Override
  public List<RDAPValidationResult> validate(ConfigurationFile config) {
    List<RDAPValidationResult> results = new ArrayList<>();
    return results;
  }

}
