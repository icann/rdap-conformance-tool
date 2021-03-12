package org.icann.rdapconformance.validator.models.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.icann.rdapconformance.validator.models.RDAPValidate;
import org.icann.rdapconformance.validator.models.entity.Entity;

public abstract class RDAPObject extends RDAPValidate {
  // TODO should be add lang: see Lang?

  // rdapConformance -- an array of strings, each providing a hint as to the specifications used in
  //                    the construction of the response.  This data structure appears only in the
  //                    topmost JSON object of a response.
  @JsonProperty
  protected List<String> rdapConformance;

  // objectClassName -- the object class name of a particular object as a string.
  @JsonProperty
  protected String objectClassName;

  // handle -- a string representing a registry unique identifier of the object
  @JsonProperty
  protected Object handle;

  // status -- an array of strings indicating the state of the IP network
  @JsonProperty
  // TODO List enum?
  protected List<String> status;

  // entities -- an array of entity objects
  @JsonProperty
  protected List<Entity> entities;

  // remarks --  Information about the object class that contains the response.
  @JsonProperty
  protected List<NoticeAndRemark> remarks;

  // notices -- Information about the service providing RDAP information and/or information
  //            about the entire response.
  @JsonProperty
  protected List<NoticeAndRemark> notices;

  // links -- signify links to other resources on the Internet.
  @JsonProperty
  protected List<Link> links;

  // port43 -- a simple string containing the fully qualified host name or IP address of the WHOIS server where the
  //           containing object instance may be found.
  @JsonProperty
  protected String port43;

  // events -- represents events that have occurred on an instance of an object class.
  @JsonProperty
  protected List<Event> events;

  @Override
  public boolean validate() {
    return true;
  }

}
