package org.icann.rdapconformance.validator.workflow;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Utility class providing a shared, thread-safe ObjectMapper instance.
 * This reduces the overhead of creating multiple ObjectMapper instances throughout the application.
 * 
 * ObjectMapper is thread-safe after configuration, so a single instance can be safely 
 * shared across all JSON operations.
 */
public class JsonMapperUtil {
    
    private static final ObjectMapper SHARED_MAPPER;
    
    static {
        SHARED_MAPPER = new ObjectMapper();
        
        // Configure the mapper with commonly used settings
        SHARED_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        SHARED_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        SHARED_MAPPER.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        
        // Additional configuration for better performance and compatibility
        SHARED_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    
    /**
     * Gets the shared ObjectMapper instance.
     * This instance is thread-safe and configured for the RDAP validator's needs.
     * 
     * @return the shared ObjectMapper instance
     */
    public static ObjectMapper getSharedMapper() {
        return SHARED_MAPPER;
    }
    
    /**
     * Gets a new ObjectMapper instance with the same configuration as the shared one.
     * Use this only when you need to modify the mapper configuration for specific use cases.
     * 
     * @return a new ObjectMapper instance with standard configuration
     */
    public static ObjectMapper createNewMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}