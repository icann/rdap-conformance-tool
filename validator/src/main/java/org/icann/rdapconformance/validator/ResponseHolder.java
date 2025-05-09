package org.icann.rdapconformance.validator;

import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton class to hold the most recent RDAP response for global access
 */
public class ResponseHolder {
    private static final ResponseHolder INSTANCE = new ResponseHolder();
    private static final Logger logger = LoggerFactory.getLogger(ResponseHolder.class);

    private RDAPHttpRequest.SimpleHttpResponse currentResponse;

    private ResponseHolder() {
        // Private constructor to prevent instantiation
    }

    public static ResponseHolder getInstance() {
        return INSTANCE;
    }

    /**
     * Set the current response in the holder
     *
     * @param response The response to store
     */
    public synchronized void setCurrentResponse(RDAPHttpRequest.SimpleHttpResponse response) {
        this.currentResponse = response;
        logger.debug("Stored new response with tracking ID: {}",
            response != null ? response.getTrackingId() : "null");
    }

    /**
     * Get the currently stored response
     *
     * @return The current response, or null if none is set
     */
    public synchronized RDAPHttpRequest.SimpleHttpResponse getCurrentResponse() {
        return this.currentResponse;
    }

    /**
     * Get the tracking ID of the currently stored response
     *
     * @return The tracking ID, or null if no response is stored
     */
    public synchronized String getCurrentResponseTrackingId() {
        return currentResponse != null ? currentResponse.getTrackingId() : null;
    }

    /**
     * Reset the response holder
     */
    public synchronized void reset() {
        this.currentResponse = null;
    }
}