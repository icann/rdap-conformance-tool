package org.icann.rdapconformance.tool.progress;

/**
 * Enum representing the different phases of RDAP validation execution.
 */
public enum ProgressPhase {
    DATASET_DOWNLOAD("DatasetDownload"),
    DATASET_PARSE("DatasetParse"),
    DNS_RESOLUTION("DNSResolution"),
    NETWORK_VALIDATION("NetworkValidation"),
    RESULTS_GENERATION("ResultsGeneration"),
    COMPLETED("Completed");

    private final String displayName;

    ProgressPhase(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}