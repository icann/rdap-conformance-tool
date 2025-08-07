package org.icann.rdapconformance.validator;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConnectionStatusTest {

    @Test
    public void testEnumValues() {
        ConnectionStatus[] values = ConnectionStatus.values();
        
        assertThat(values).hasSize(15);
        assertThat(values).contains(
            ConnectionStatus.SUCCESS,
            ConnectionStatus.CONNECTION_FAILED,
            ConnectionStatus.HANDSHAKE_FAILED,
            ConnectionStatus.INVALID_CERTIFICATE,
            ConnectionStatus.REVOKED_CERTIFICATE,
            ConnectionStatus.EXPIRED_CERTIFICATE,
            ConnectionStatus.CERTIFICATE_ERROR,
            ConnectionStatus.TOO_MANY_REDIRECTS,
            ConnectionStatus.HTTP_ERROR,
            ConnectionStatus.HTTP2_ERROR,
            ConnectionStatus.NETWORK_SEND_FAIL,
            ConnectionStatus.NETWORK_RECEIVE_FAIL,
            ConnectionStatus.UNKNOWN_HOST,
            ConnectionStatus.CONNECTION_REFUSED,
            ConnectionStatus.TOO_MANY_REQUESTS
        );
    }
    
    @Test
    public void testSuccess() {
        ConnectionStatus status = ConnectionStatus.SUCCESS;
        
        assertThat(status.getCode()).isEqualTo(0);
        assertThat(status.getDescription()).isEqualTo("Connection succeeded");
    }
    
    @Test
    public void testConnectionFailed() {
        ConnectionStatus status = ConnectionStatus.CONNECTION_FAILED;
        
        assertThat(status.getCode()).isEqualTo(10);
        assertThat(status.getDescription()).isEqualTo("Failed to connect to host");
    }
    
    @Test
    public void testHandshakeFailed() {
        ConnectionStatus status = ConnectionStatus.HANDSHAKE_FAILED;
        
        assertThat(status.getCode()).isEqualTo(11);
        assertThat(status.getDescription()).isEqualTo("The TLS handshake failed");
    }
    
    @Test
    public void testInvalidCertificate() {
        ConnectionStatus status = ConnectionStatus.INVALID_CERTIFICATE;
        
        assertThat(status.getCode()).isEqualTo(12);
        assertThat(status.getDescription()).contains("common name invalid");
    }
    
    @Test
    public void testRevokedCertificate() {
        ConnectionStatus status = ConnectionStatus.REVOKED_CERTIFICATE;
        
        assertThat(status.getCode()).isEqualTo(13);
        assertThat(status.getDescription()).contains("revoked");
    }
    
    @Test
    public void testExpiredCertificate() {
        ConnectionStatus status = ConnectionStatus.EXPIRED_CERTIFICATE;
        
        assertThat(status.getCode()).isEqualTo(14);
        assertThat(status.getDescription()).contains("expired");
    }
    
    @Test
    public void testCertificateError() {
        ConnectionStatus status = ConnectionStatus.CERTIFICATE_ERROR;
        
        assertThat(status.getCode()).isEqualTo(15);
        assertThat(status.getDescription()).contains("Other errors");
    }
    
    @Test
    public void testTooManyRedirects() {
        ConnectionStatus status = ConnectionStatus.TOO_MANY_REDIRECTS;
        
        assertThat(status.getCode()).isEqualTo(16);
        assertThat(status.getDescription()).isEqualTo("Too many redirects");
    }
    
    @Test
    public void testHttpError() {
        ConnectionStatus status = ConnectionStatus.HTTP_ERROR;
        
        assertThat(status.getCode()).isEqualTo(17);
        assertThat(status.getDescription()).isEqualTo("HTTP errors");
    }
    
    @Test
    public void testHttp2Error() {
        ConnectionStatus status = ConnectionStatus.HTTP2_ERROR;
        
        assertThat(status.getCode()).isEqualTo(18);
        assertThat(status.getDescription()).isEqualTo("HTTP/2 errors");
    }
    
    @Test
    public void testNetworkSendFail() {
        ConnectionStatus status = ConnectionStatus.NETWORK_SEND_FAIL;
        
        assertThat(status.getCode()).isEqualTo(19);
        assertThat(status.getDescription()).contains("sending network data");
    }
    
    @Test
    public void testNetworkReceiveFail() {
        ConnectionStatus status = ConnectionStatus.NETWORK_RECEIVE_FAIL;
        
        assertThat(status.getCode()).isEqualTo(20);
        assertThat(status.getDescription()).contains("receiving network data");
    }
    
    @Test
    public void testUnknownHost() {
        ConnectionStatus status = ConnectionStatus.UNKNOWN_HOST;
        
        assertThat(status.getCode()).isEqualTo(23);
        assertThat(status.getDescription()).isEqualTo("Unknown host");
    }
    
    @Test
    public void testConnectionRefused() {
        ConnectionStatus status = ConnectionStatus.CONNECTION_REFUSED;
        
        assertThat(status.getCode()).isEqualTo(24);
        assertThat(status.getDescription()).isEqualTo("Connection refused by host");
    }
    
    @Test
    public void testTooManyRequests() {
        ConnectionStatus status = ConnectionStatus.TOO_MANY_REQUESTS;
        
        assertThat(status.getCode()).isEqualTo(26);
        assertThat(status.getDescription()).isEqualTo("Too many requests");
    }
    
    @Test
    public void testConformanceErrorInterface() {
        ConnectionStatus status = ConnectionStatus.SUCCESS;
        
        assertThat(status).isInstanceOf(ConformanceError.class);
        assertThat(((ConformanceError) status).getCode()).isEqualTo(0);
    }
    
    @Test
    public void testCodeUniqueness() {
        ConnectionStatus[] values = ConnectionStatus.values();
        
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                assertThat(values[i].getCode())
                    .as("Codes must be unique: %s vs %s", values[i], values[j])
                    .isNotEqualTo(values[j].getCode());
            }
        }
    }
    
    @Test
    public void testValueOf() {
        assertThat(ConnectionStatus.valueOf("SUCCESS"))
            .isEqualTo(ConnectionStatus.SUCCESS);
        assertThat(ConnectionStatus.valueOf("CONNECTION_FAILED"))
            .isEqualTo(ConnectionStatus.CONNECTION_FAILED);
    }
    
    @Test
    public void testValueOf_InvalidName_ThrowsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> ConnectionStatus.valueOf("INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testDescriptionNotEmpty() {
        for (ConnectionStatus status : ConnectionStatus.values()) {
            assertThat(status.getDescription())
                .as("Description should not be empty for %s", status)
                .isNotEmpty();
        }
    }
    
    @Test
    public void testNoCode21() {
        for (ConnectionStatus status : ConnectionStatus.values()) {
            assertThat(status.getCode())
                .as("Code 21 is reserved for FILE_WRITE_ERROR in ToolResult")
                .isNotEqualTo(21);
        }
    }
}