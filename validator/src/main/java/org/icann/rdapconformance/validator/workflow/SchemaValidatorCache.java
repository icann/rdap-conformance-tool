package org.icann.rdapconformance.validator.workflow;

import java.util.concurrent.ConcurrentHashMap;
import org.everit.json.schema.Schema;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchemaValidatorCache {

  private static final Logger logger = LoggerFactory.getLogger(SchemaValidatorCache.class);
  
  // Cache for compiled Schema objects keyed by schema name + dataset service hash
  // Use single-threaded access to reduce contention - schemas are typically loaded once
  private static final ConcurrentHashMap<String, Schema> schemaCache = new ConcurrentHashMap<>(32, 0.75f, 1);
  
  // Padding to prevent false sharing with other caches
  private static final long[] padding2 = new long[8];
  
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
    return getCachedValidator(schemaName, results, datasetService, null);
  }

  public static SchemaValidator getCachedValidator(String schemaName,
                                                   RDAPValidatorResults results,
                                                   RDAPDatasetService datasetService,
                                                   org.icann.rdapconformance.validator.QueryContext queryContext) {
    // Create cache key based on schema name and dataset service
    String cacheKey = createCacheKey(schemaName, datasetService);

    // Fast path: check cache without locks
    Schema cachedSchema = schemaCache.get(cacheKey);
    if (cachedSchema != null) {
      logger.debug("Using cached Schema for schema: {}", schemaName);
      return createValidatorWithSchema(cachedSchema, results, queryContext);
    }

    // Lock-free schema creation using computeIfAbsent
    Schema newSchema = schemaCache.computeIfAbsent(cacheKey, key -> {
      // Check cache size and evict if necessary (non-blocking)
      if (schemaCache.size() >= MAX_CACHE_SIZE) {
        // Simple random eviction to avoid blocking
        schemaCache.entrySet().removeIf(entry -> Math.random() < 0.2);
      }

      // Create new schema
      logger.debug("Creating new Schema for schema: {}", schemaName);
      return SchemaValidator.getSchema(schemaName, "json-schema/",
                                      SchemaValidatorCache.class.getClassLoader(),
                                      datasetService);
    });

    return createValidatorWithSchema(newSchema, results, queryContext);
  }
  
  private static SchemaValidator createValidatorWithSchema(Schema schema, RDAPValidatorResults results) {
    return createValidatorWithSchema(schema, results, null);
  }

  private static SchemaValidator createValidatorWithSchema(Schema schema, RDAPValidatorResults results, org.icann.rdapconformance.validator.QueryContext queryContext) {
    // Create a SchemaValidator using the cached schema
    // Now uses QueryContext-aware constructor when available
    if (queryContext != null) {
      return new SchemaValidator(schema, results, queryContext);
    } else {
      return new SchemaValidator(schema, results);
    }
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
    schemaCache.clear();
    logger.debug("Schema cache cleared");
  }
  
  /**
   * Gets the current cache size.
   * 
   * @return the number of cached schemas
   */
  public static int getCacheSize() {
    return schemaCache.size();
  }
}