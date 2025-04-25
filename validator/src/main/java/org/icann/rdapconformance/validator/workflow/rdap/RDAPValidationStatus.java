package org.icann.rdapconformance.validator.workflow.rdap;

public enum RDAPValidationStatus {
  SUCCESS(0, "A response was available to the tool, the Content-Type is application/rdap+JSON "
      + "in the response, a HTTP Status of 200 or 404 was received, the RDAP response was "
      + "successfully parsed and the results file was generated."),
  CONFIG_INVALID(1, "The configuration definition file is syntactically invalid."),
  DATASET_UNAVAILABLE(2, "The tool was not able to download a dataset."),
  UNSUPPORTED_QUERY(3, "The RDAP query is not supported by the tool."),
  MIXED_LABEL_FORMAT(4, "The RDAP query is domain/<domain name> or nameserver/<nameserver>, "
      + "but A-labels and U-labels are mixed."),
  USES_THIN_MODEL(9, "The RDAP query is invalid because the TLD uses the thin model."),
// TODO: these are our legacy connection issues - which don't apply anymore and the ones that do are now moved to result errors
  FILE_WRITE_ERROR(21, "Failure in writing to results file");

  private final int value;
  private final String description;

  RDAPValidationStatus(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }

  public static RDAPValidationStatus fromValue(int value) {
    for (RDAPValidationStatus status : values()) {
      if (status.value == value) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown value: " + value);
  }
}
