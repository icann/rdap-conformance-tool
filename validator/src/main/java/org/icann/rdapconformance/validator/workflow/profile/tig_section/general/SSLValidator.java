package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.util.List;

/**
 * Interface for SSL/TLS validation operations.
 * This allows for easier testing by providing a mockable abstraction
 * over the complex SSL socket operations.
 */
public interface SSLValidator {
    
    /**
     * Validates the SSL/TLS configuration of a server.
     * 
     * @param hostname The hostname to connect to
     * @param port The port to connect to
     * @return SSLValidationResult containing the validation outcome.
     *         This method will never return null - it always returns either a
     *         success or failure result.
     */
    SSLValidationResult validateSSL(String hostname, int port);
    
    /**
     * Validates TLS 1.2 cipher suites for a server.
     * 
     * @param hostname The hostname to connect to
     * @param port The port to connect to
     * @return CipherValidationResult containing the cipher validation outcome.
     *         This method will never return null - it always returns either a
     *         success or failure result.
     */
    CipherValidationResult validateTLS12CipherSuites(String hostname, int port);

    /**
     * Connects to the server and checks whether the leaf TLS certificate presented by the
     * server has expired (gTLD Profile v3 test case -66009).
     */
    CertificateExpirationResult checkCertificateExpiration(String hostname, int port);

    class CertificateExpirationResult {
        private final boolean successful;
        private final boolean expired;
        private final String errorMessage;

        public CertificateExpirationResult(boolean successful, boolean expired, String errorMessage) {
            this.successful = successful;
            this.expired = expired;
            this.errorMessage = errorMessage;
        }

        public static CertificateExpirationResult valid() {
            return new CertificateExpirationResult(true, false, null);
        }

        public static CertificateExpirationResult expired() {
            return new CertificateExpirationResult(true, true, null);
        }

        public static CertificateExpirationResult failure(String errorMessage) {
            return new CertificateExpirationResult(false, false, errorMessage);
        }

        /** True when the certificate could be retrieved and inspected. */
        public boolean isSuccessful() {
            return successful;
        }

        public boolean isExpired() {
            return expired;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
    
    /**
     * Result of SSL validation containing the protocols and cipher information.
     */
    class SSLValidationResult {
        private final boolean successful;
        private final List<String> enabledProtocols;
        private final String errorMessage;
        private final Exception exception;
        
        public SSLValidationResult(boolean successful, List<String> enabledProtocols, String errorMessage, Exception exception) {
            this.successful = successful;
            this.enabledProtocols = enabledProtocols;
            this.errorMessage = errorMessage;
            this.exception = exception;
        }
        
        public static SSLValidationResult success(List<String> enabledProtocols) {
            return new SSLValidationResult(true, enabledProtocols, null, null);
        }
        
        public static SSLValidationResult failure(String errorMessage, Exception exception) {
            return new SSLValidationResult(false, null, errorMessage, exception);
        }
        
        public boolean isSuccessful() {
            return successful;
        }
        
        public List<String> getEnabledProtocols() {
            return enabledProtocols;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public Exception getException() {
            return exception;
        }
    }
    
    /**
     * Result of cipher suite validation.
     */
    class CipherValidationResult {
        private final boolean successful;
        private final String cipherSuite;
        private final String protocol;
        private final String errorMessage;
        private final Exception exception;
        
        public CipherValidationResult(boolean successful, String cipherSuite, String protocol, String errorMessage, Exception exception) {
            this.successful = successful;
            this.cipherSuite = cipherSuite;
            this.protocol = protocol;
            this.errorMessage = errorMessage;
            this.exception = exception;
        }
        
        public static CipherValidationResult success(String protocol, String cipherSuite) {
            return new CipherValidationResult(true, cipherSuite, protocol, null, null);
        }
        
        public static CipherValidationResult failure(String errorMessage, Exception exception) {
            return new CipherValidationResult(false, null, null, errorMessage, exception);
        }
        
        public boolean isSuccessful() {
            return successful;
        }
        
        public String getCipherSuite() {
            return cipherSuite;
        }
        
        public String getProtocol() {
            return protocol;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public Exception getException() {
            return exception;
        }
    }
}