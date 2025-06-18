package org.icann.rdapconformance.validator;


/**
 * Represents the status of a network connection or connection attempt.
 */
public enum ConnectionStatus implements ConformanceError {
    // NOTE: these no longer have anything to do with the application exiting.
    SUCCESS(0, "Connection succeeded"),
    CONNECTION_FAILED(10, "Failed to connect to host"),
    HANDSHAKE_FAILED(11, "The TLS handshake failed"),
    INVALID_CERTIFICATE(12, "TLS server certificate - common name invalid"),
    REVOKED_CERTIFICATE(13, "TLS server certificate - revoked"),
    EXPIRED_CERTIFICATE(14, "TLS server certificate - expired"),
    CERTIFICATE_ERROR(15, "Other errors with the TLS server certificate"),
    TOO_MANY_REDIRECTS(16, "Too many redirects"),
    HTTP_ERROR(17, "HTTP errors"),
    HTTP2_ERROR(18, "HTTP/2 errors"),
    NETWORK_SEND_FAIL(19, "Failure sending network data"),
    NETWORK_RECEIVE_FAIL(20, "Failure in receiving network data"),
    // 21 is over in ToolResult as FILE_WRITE_ERROR, legacy exit numbers
    UNKNOWN_HOST(23, "Unknown host"),
    CONNECTION_REFUSED(24, "Connection refused by host"),
    TOO_MANY_REQUESTS(26, "Too many requests");

    private final int code;
    private final String description;

    ConnectionStatus(int code, String description) {
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
