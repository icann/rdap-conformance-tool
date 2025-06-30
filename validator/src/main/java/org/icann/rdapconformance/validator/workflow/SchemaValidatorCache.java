package org.icann.rdapconformance.validator.workflow;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchemaValidatorCache {

  private static final Logger logger = LoggerFactory.getLogger(SchemaValidatorCache.class);
  
  // Cache for compiled Schema objects keyed by schema name + dataset service hash
  private static final ConcurrentHashMap<String, Schema> schemaCache = new ConcurrentHashMap<>();
  
  // Lock for thread-safe cache operations
  private static final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
  
  // Maximum cache size to prevent memory leaks
  private static final int MAX_CACHE_SIZE = 50;
  
  private SchemaValidatorCache() {
    // Utility class - no instantiation
  }
  
  /**
   * Gets a SchemaValidator instance using a cached Schema object or creates a new one if not cached.
   * The compiled Schema objects are cached by schema name and dataset service hash
   * to avoid repeated schema loading and compilation, which is expensive.
   * 
   * @param schemaName the name of the schema file
   * @param results the results object for validation
   * @param datasetService the dataset service for validation
   * @return a SchemaValidator instance using cached or new Schema
   */
  public static SchemaValidator getCachedValidator(String schemaName, 
                                                   RDAPValidatorResults results, 
                                                   RDAPDatasetService datasetService) {
    // Create cache key based on schema name and dataset service
    String cacheKey = createCacheKey(schemaName, datasetService);
    
    cacheLock.readLock().lock();
    try {
      Schema cachedSchema = schemaCache.get(cacheKey);
      if (cachedSchema != null) {
        logger.debug("Using cached Schema for schema: {}", schemaName);
        return createValidatorWithSchema(cachedSchema, results);
      }
    } finally {
      cacheLock.readLock().unlock();
    }
    
    // Not in cache, create new schema
    cacheLock.writeLock().lock();
    try {
      // Double-check pattern - another thread might have added it
      Schema cachedSchema = schemaCache.get(cacheKey);
      if (cachedSchema != null) {
        logger.debug("Using cached Schema for schema: {} (added by another thread)", schemaName);
        return createValidatorWithSchema(cachedSchema, results);
      }
      
      // Check cache size and evict if necessary
      if (schemaCache.size() >= MAX_CACHE_SIZE) {
        evictOldestEntry();
      }
      
      // Create new schema and cache it
      logger.debug("Creating new Schema for schema: {}", schemaName);
      Schema newSchema = SchemaValidator.getSchema(schemaName, "json-schema/", 
                                                  SchemaValidatorCache.class.getClassLoader(), 
                                                  datasetService);
      schemaCache.put(cacheKey, newSchema);
      
      return createValidatorWithSchema(newSchema, results);
    } finally {
      cacheLock.writeLock().unlock();
    }
  }
  
  private static SchemaValidator createValidatorWithSchema(Schema schema, RDAPValidatorResults results) {
    // Create a SchemaValidator using the cached schema
    // We need to use reflection or create a constructor that accepts a pre-built schema
    // For now, we'll fall back to the standard constructor since SchemaValidator doesn't expose this
    // This is still beneficial because the expensive schema loading is cached
    return new SchemaValidator(schema, results);
  }
  
  private static String createCacheKey(String schemaName, RDAPDatasetService datasetService) {
    // Create a cache key that includes schema name and dataset service hashcode
    // This ensures that different dataset configurations don't share schemas
    return schemaName + "_" + System.identityHashCode(datasetService);
  }
  
  private static void evictOldestEntry() {
    // Simple eviction strategy - remove the first entry
    // In a production system, you might want LRU eviction
    if (!schemaCache.isEmpty()) {
      String firstKey = schemaCache.keys().nextElement();
      schemaCache.remove(firstKey);
      logger.debug("Evicted schema from cache: {}", firstKey);
    }
  }
  
  /**
   * Clears the schema cache. Mainly for testing purposes.
   */
  public static void clearCache() {
    cacheLock.writeLock().lock();
    try {
      schemaCache.clear();
      logger.debug("Schema cache cleared");
    } finally {
      cacheLock.writeLock().unlock();
    }
  }
  
  /**
   * Gets the current cache size.
   * 
   * @return the number of cached schemas
   */
  public static int getCacheSize() {
    cacheLock.readLock().lock();
    try {
      return schemaCache.size();
    } finally {
      cacheLock.readLock().unlock();
    }
  }
}