package org.icann.rdapconformance.validator;

/**
 * Interface for receiving progress updates during dataset operations.
 */
public interface ProgressCallback {
    
    /**
     * Called when a dataset download starts.
     * @param datasetName The name of the dataset being downloaded
     */
    void onDatasetDownloadStarted(String datasetName);
    
    /**
     * Called when a dataset download completes successfully.
     * @param datasetName The name of the dataset that was downloaded
     */
    void onDatasetDownloadCompleted(String datasetName);
    
    /**
     * Called when a dataset parse starts.
     * @param datasetName The name of the dataset being parsed
     */
    void onDatasetParseStarted(String datasetName);
    
    /**
     * Called when a dataset parse completes successfully.
     * @param datasetName The name of the dataset that was parsed
     */
    void onDatasetParseCompleted(String datasetName);
    
    /**
     * Called when a dataset operation fails.
     * @param datasetName The name of the dataset that failed
     * @param operation The operation that failed ("download" or "parse")
     * @param error The error that occurred
     */
    void onDatasetError(String datasetName, String operation, Throwable error);
}