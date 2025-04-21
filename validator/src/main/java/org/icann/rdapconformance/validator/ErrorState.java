package org.icann.rdapconformance.validator;

import java.util.HashMap;
import java.util.Map;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;

public class ErrorState {
    private static final ErrorState INSTANCE = new ErrorState();
    private final Map<String, ErrorDetails> errorInfo;

    // Private constructor for Singleton
    private ErrorState() {
        this.errorInfo = new HashMap<>();
    }

    // Get the Singleton instance
    public static ErrorState getInstance() {
        return INSTANCE;
    }

    // Add error information for a specific state
    public synchronized void addErrorInfo(int errorCode, String uri, int redirects) {
        String stack = NetworkInfo.getNetworkProtocolAsString();
        String header = NetworkInfo.getAcceptHeader();
        String ipAddress = NetworkInfo.getServerIpAddress();
        String errorName = RDAPValidationStatus.fromValue(errorCode).name();
        String key = "ErrorCode: " + errorCode + " (" + errorName + ")\n" +
            "Header: " + header + "\n" +
            "Stack: " + stack;

        if (!errorInfo.containsKey(key)) {
            errorInfo.put(key, new ErrorDetails(uri, ipAddress, redirects, 1));
        } else {
            errorInfo.get(key).incrementCount();
        }
    }

    @Override
    public synchronized String toString() {
        if (errorInfo.isEmpty()) {
            return "No errors collected.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Collected Error States:\n");
        for (Map.Entry<String, ErrorDetails> entry : errorInfo.entrySet()) {
            if (entry.getKey().contains("ErrorCode: 0 (SUCCESS)")) {
                continue; // Skip success states if not needed
            }
            ErrorDetails details = entry.getValue();
            sb.append(entry.getKey()).append("\n")
              .append("URI: ").append(details.uri).append("\n")
              .append("IP Address: ").append(details.ipAddress).append("\n")
              .append("Redirects: ").append(details.redirects).append("\nCount: ").append(details.count).append("\n\n");
        }
        return sb.toString().trim();
    }

    // Inner class to store error details
    private static class ErrorDetails {
        private final String uri;
        private final String ipAddress;
        private final int redirects;
        private int count;

        public ErrorDetails(String uri, String ipAddress, int redirects, int count) {
            this.uri = uri;
            this.ipAddress = ipAddress;
            this.redirects = redirects;
            this.count = count;
        }

        public void incrementCount() {
            this.count++;
        }
    }
}