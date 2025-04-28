package org.icann.rdapconformance.validator;

/**
 * Represents the result of RDAP validation tests.
 */
public enum ToolResult {
    SUCCESS(0, "A response was available to the tool, the Content-Type is application/rdap+JSON "
        + "in the response, a HTTP Status of 200 or 404 was received, the RDAP response was "
        + "successfully parsed and the results file was generated"),
    CONFIG_INVALID(1, "The configuration definition file is syntactically invalid"),
    DATASET_UNAVAILABLE(2, "The tool was not able to download a dataset"),
    UNSUPPORTED_QUERY(3, "The RDAP query is not supported by the tool"),
    MIXED_LABEL_FORMAT(4,
        "The RDAP query is domain/<domain name> or nameserver/<nameserver>, " + "but A-labels and U-labels are mixed"),
    USES_THIN_MODEL(9, "The RDAP query is invalid because the TLD uses the thin model"),
    FILE_WRITE_ERROR(21, "Failure in writing to results file");

    private final int code;
    private final String description;

    ToolResult(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}