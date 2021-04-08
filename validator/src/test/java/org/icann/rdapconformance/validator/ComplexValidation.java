package org.icann.rdapconformance.validator;

class ComplexValidation {

  public ComplexValidation(String validationName, String validatedField, int errorCode) {
    this.validationName = validationName;
    this.validatedField = validatedField;
    this.errorCode = errorCode;
  }

  public String validationName;
  public String validatedField;
  public int errorCode;

  public static ComplexValidation ofRoles(int errorCode) {
    return new ComplexValidation("stdRdapRolesValidation", "roles", errorCode);
  }

  public static ComplexValidation ofPublicIds(int errorCode) {
    return new ComplexValidation("stdRdapPublicIdsValidation", "publicIds", errorCode);
  }

  public static ComplexValidation ofEntities(int errorCode) {
    return new ComplexValidation("stdRdapEntitiesValidation", "entities", errorCode);
  }

  public static ComplexValidation ofRemarks(int errorCode) {
    return new ComplexValidation("stdRdapRemarksValidation", "remarks", errorCode);
  }

  public static ComplexValidation ofLinks(int errorCode) {
    return new ComplexValidation("stdRdapLinksValidation", "links", errorCode);
  }

  public static ComplexValidation ofEvents(int errorCode) {
    return new ComplexValidation("stdRdapEventsValidation", "events", errorCode);
  }

  public static ComplexValidation ofAsEventActor(int errorCode) {
    return new ComplexValidation("stdRdapAsEventActorValidation", "asEventActor", errorCode);
  }

  public static ComplexValidation ofStatus(int errorCode) {
    return new ComplexValidation("stdRdapStatusValidation", "status", errorCode);
  }

  public static ComplexValidation ofPort43(int errorCode) {
    return new ComplexValidation("stdRdapPort43WhoisServerValidation", "port43", errorCode);
  }

  public static ComplexValidation ofNotices(int errorCode) {
    return new ComplexValidation("stdRdapNoticesRemarksValidation", "notices", errorCode);
  }

  public static ComplexValidation ofRdapConformance(int errorCode) {
    return new ComplexValidation("stdRdapConformanceValidation", "rdapConformance", errorCode);
  }

  public static ComplexValidation ofUnicodeName(int errorCode) {
    return new ComplexValidation("stdRdapUnicodeNameValidation", "unicodeName", errorCode);
  }

  public static ComplexValidation ofLdhName(int errorCode) {
    return new ComplexValidation("stdRdapLdhNameValidation", "ldhName", errorCode);
  }
}
