package org.icann.rdap.conformance.validator;

enum RDAPValidationStatus {
  SUCCESS(0, "A response was available to the tool, the Content-Type is application/rdap+JSON "
      + "in the response, a HTTP Status of 200 or 404 was received, the RDAP response was "
      + "successfully parsed and the results file was generated."),
  CONFIG_INVALID(1, "The configuration definition file is syntactically invalid."),
  DATASET_UNAVAILABLE(2, "The tool was not able to download a dataset."),
  UNSUPPORTED_QUERY(3, "The RDAP query is not supported by the tool."),
  MIXED_LABEL_FORMAT(4, "The RDAP query is domain/<domain name> or nameserver/<nameserver>, "
      + "but A-labels and U-labels are mixed."),
  WRONG_CONTENT_TYPE(5, "A response was available to the tool, but the Content-Type is not "
      + "application/rdap+JSON in the response."),
  RESPONSE_INVALID(6, "A response was available to the tool, the Content-Type is "
      + "application/rdap+JSON in the response, but the RDAP response is not a syntactically "
      + "valid JSON object."),
  INVALID_HTTP_STATUS(7, "A response was available to the tool, the Content-Type is "
      + "application/rdap+JSON in the response, but the HTTP Status is not 200 nor 404."),
  EXPECTED_OBJECT_NOT_FOND(8, "A response was available to the tool, the Content-Type is "
      + "application/rdap+JSON in the response, the HTTP Status is 200 or 404, but the expected "
      + "objectClassName in the topmost object was not found for a lookup query or the JSON "
      + "array for a search query was not found a search query."),
  USES_THIN_MODEL(9, "The RDAP query is invalid because the TLD uses the thin model."),
  CONNECTION_FAILED(10, "Failed to connect to host."),
  HANDSHAKE_FAILED(11, "The TLS handshake failed."),
  INVALID_CERTIFICATE(12, "TLS server certificate - common name invalid."),
  REVOKED_CERTIFICATE(13, "TLS server certificate - revoked."),
  EXPIRED_CERTIFICATE(14, "TLS server certificate - expired."),
  CERTIFICATE_ERROR(15, "Other errors with the TLS server certificate."),
  TOO_MANY_REDIRECTS(16, "Too many redirects."),
  HTTP_ERROR(17, "HTTP errors."),
  HTTP2_ERROR(18, "HTTP/2 errors."),
  NETWORK_SEND_FAIL(19, "Failure sending network data."),
  NETWORK_RECEIVE_FAIL(20, "Failure in receiving network data.");


  private final int value;
  private final String description;

  RDAPValidationStatus(int value, String description) {
    this.value = value;
    this.description = description;
  }

  public int getValue() {
    return value;
  }
}
