package org.icann.rdap.conformance.validator;

enum RDAPValidationStatus {
  SUCCESS(1, "A response was available to the tool, the Content-Type is application/rdap+JSON "
      + "in the response, a HTTP Status of 200 or 404 was received, the RDAP response was "
      + "successfully parsed and the results file was generated."),
  CONFIG_INVALID(2, "The configuration definition file is syntactically invalid."),
  DATASET_UNAVAILABLE(3, "The tool was not able to download a dataset."),
  UNSUPPORTED_QUERY(4, "The RDAP query is not supported by the tool."),
  MIXED_LABEL_FORMAT(5, "The RDAP query is domain/<domain name> or nameserver/<nameserver>, "
      + "but A-labels and U-labels are mixed."),
  WRONG_CONTENT_TYPE(6, "A response was available to the tool, but the Content-Type is not "
      + "application/rdap+JSON in the response."),
  RESPONSE_INVALID(7, "A response was available to the tool, the Content-Type is "
      + "application/rdap+JSON in the response, but the RDAP response is not a syntactically "
      + "valid JSON object."),
  INVALID_HTTP_STATUS(8, "A response was available to the tool, the Content-Type is "
      + "application/rdap+JSON in the response, but the HTTP Status is not 200 nor 404."),
  EXPECTED_OBJECT_NOT_FOND(9, "A response was available to the tool, the Content-Type is "
      + "application/rdap+JSON in the response, the HTTP Status is 200 or 404, but the expected "
      + "objectClassName in the topmost object was not found for a lookup query or the JSON "
      + "array for a search query was not found a search query."),
  USES_THIN_MODEL(10, "The RDAP query is invalid because the TLD uses the thin model."),
  CONNECTION_FAILED(11, "Failed to connect to host."),
  HANDSHAKE_FAILED(12, "The TLS handshake failed."),
  INVALID_CERTIFICATE(13, "TLS server certificate - common name invalid."),
  REVOKED_CERTIFICATE(14, "TLS server certificate - revoked."),
  EXPIRED_CERTIFICATE(15, "TLS server certificate - expired."),
  CERTIFICATE_ERROR(16, "Other errors with the TLS server certificate."),
  TOO_MANY_REDIRECTS(17, "Too many redirects."),
  HTTP_ERROR(18, "HTTP errors."),
  HTTP2_ERROR(19, "HTTP/2 errors."),
  NETWORK_SEND_FAIL(20, "Failure sending network data."),
  NETWORK_RECEIVE_FAIL(21, "Failure in receiving network data.");


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
