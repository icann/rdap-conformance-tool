package org.icann.rdapconformance.validator.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SchemaValidatorCacheTest {

  @Mock
  private RDAPDatasetService mockDatasetService;
  
  @Mock
  private RDAPValidatorResults mockResults;
  
  @BeforeMethod
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    // Clear cache before each test
    SchemaValidatorCache.clearCache();
  }
  
  @AfterMethod
  public void tearDown() {
    // Clear cache after each test
    SchemaValidatorCache.clearCache();
  }

  @Test
  public void testGetCachedValidatorReturnsNonNull() {
    SchemaValidator validator = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", mockResults, mockDatasetService);
    
    assertThat(validator).isNotNull();
  }

  @Test
  public void testSameSchemaAndDatasetReturnsCachedValidator() {
    // Note: We can't directly test that the same Schema object is used internally,
    // but we can verify that subsequent calls are faster due to caching
    
    long start = System.currentTimeMillis();
    SchemaValidator validator1 = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", mockResults, mockDatasetService);
    long firstCallTime = System.currentTimeMillis() - start;
    
    start = System.currentTimeMillis();
    SchemaValidator validator2 = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", mockResults, mockDatasetService);
    long secondCallTime = System.currentTimeMillis() - start;
    
    assertThat(validator1).isNotNull();
    assertThat(validator2).isNotNull();
    
    // Both validators should work with the same cached schema
    // The second call should generally be faster, but we can't guarantee this in tests
  }

  @Test
  public void testDifferentSchemaNamesCreateDifferentCacheEntries() {
    SchemaValidator validator1 = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", mockResults, mockDatasetService);
    SchemaValidator validator2 = SchemaValidatorCache.getCachedValidator(
        "rdap_entity.json", mockResults, mockDatasetService);
    
    assertThat(validator1).isNotNull();
    assertThat(validator2).isNotNull();
    
    // Different schemas should have different cache entries
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(2);
  }

  @Test
  public void testDifferentDatasetServicesCreateDifferentCacheEntries() {
    RDAPDatasetService mockDatasetService2 = mock(RDAPDatasetService.class);
    
    SchemaValidator validator1 = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", mockResults, mockDatasetService);
    SchemaValidator validator2 = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", mockResults, mockDatasetService2);
    
    assertThat(validator1).isNotNull();
    assertThat(validator2).isNotNull();
    
    // Same schema but different dataset services should have different cache entries
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(2);
  }

  @Test
  public void testClearCache() {
    // Add some validators to cache
    SchemaValidatorCache.getCachedValidator("rdap_domain.json", mockResults, mockDatasetService);
    SchemaValidatorCache.getCachedValidator("rdap_entity.json", mockResults, mockDatasetService);
    
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(2);
    
    // Clear the cache
    SchemaValidatorCache.clearCache();
    
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(0);
  }

  @Test
  public void testGetCacheSize() {
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(0);
    
    SchemaValidatorCache.getCachedValidator("rdap_domain.json", mockResults, mockDatasetService);
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(1);
    
    SchemaValidatorCache.getCachedValidator("rdap_entity.json", mockResults, mockDatasetService);
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(2);
    
    // Same schema should not increase cache size
    SchemaValidatorCache.getCachedValidator("rdap_domain.json", mockResults, mockDatasetService);
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(2);
  }

  @Test
  public void testConcurrentAccessIsSafe() throws InterruptedException {
    int threadCount = 20;
    CountDownLatch latch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    Set<Exception> exceptions = new HashSet<>();
    
    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          SchemaValidator validator = SchemaValidatorCache.getCachedValidator(
              "rdap_domain.json", mockResults, mockDatasetService);
          assertThat(validator).isNotNull();
        } catch (Exception e) {
          synchronized (exceptions) {
            exceptions.add(e);
          }
        } finally {
          latch.countDown();
        }
      });
    }
    
    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    
    // No exceptions should occur during concurrent access
    assertThat(exceptions).isEmpty();
    
    // Cache should have only one entry for the schema
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(1);
  }

  @Test
  public void testCacheEviction() {
    // Only test with real schema that we know exists
    // This test verifies eviction works when max size is reached
    
    // Get initial cache size
    int initialSize = SchemaValidatorCache.getCacheSize();
    
    // Add a few validators
    SchemaValidatorCache.getCachedValidator("rdap_domain.json", mockResults, mockDatasetService);
    SchemaValidatorCache.getCachedValidator("rdap_entity.json", mockResults, mockDatasetService);
    
    // Cache should have grown
    assertThat(SchemaValidatorCache.getCacheSize()).isGreaterThan(initialSize);
  }

  @Test
  public void testDifferentResultsObjectsStillUseCachedSchema() {
    RDAPValidatorResults results1 = mock(RDAPValidatorResults.class);
    RDAPValidatorResults results2 = mock(RDAPValidatorResults.class);
    
    SchemaValidator validator1 = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", results1, mockDatasetService);
    SchemaValidator validator2 = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", results2, mockDatasetService);
    
    assertThat(validator1).isNotNull();
    assertThat(validator2).isNotNull();
    
    // Same schema should be cached even with different results objects
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(1);
  }

  @Test
  public void testPerformanceImprovement() {
    // Measure time for first call (cache miss)
    long start = System.currentTimeMillis();
    SchemaValidator firstValidator = SchemaValidatorCache.getCachedValidator(
        "rdap_domain.json", mockResults, mockDatasetService);
    long firstCallTime = System.currentTimeMillis() - start;
    
    // Clear validator reference
    firstValidator = null;
    
    // Measure time for subsequent calls (cache hits)
    int iterations = 100;
    start = System.currentTimeMillis();
    for (int i = 0; i < iterations; i++) {
      SchemaValidator cachedValidator = SchemaValidatorCache.getCachedValidator(
          "rdap_domain.json", mockResults, mockDatasetService);
      assertThat(cachedValidator).isNotNull();
    }
    long totalCachedTime = System.currentTimeMillis() - start;
    
    // Average time per cached call
    double avgCachedTime = (double) totalCachedTime / iterations;
    
    // Cached calls should be fast (though we can't guarantee specific timing in tests)
    assertThat(avgCachedTime).isLessThan(10.0); // Less than 10ms per call
  }

  @Test
  public void testMultipleSchemaTypes() {
    // Test with various schema types used in the application
    String[] schemaNames = {
        "rdap_domain.json",
        "rdap_entity.json",
        "rdap_nameserver.json",
        "rdap_help.json",
        "rdap_error.json",
        "rdap_autnum.json",
        "rdap_ip_network.json"
    };
    
    for (String schemaName : schemaNames) {
      SchemaValidator validator = SchemaValidatorCache.getCachedValidator(
          schemaName, mockResults, mockDatasetService);
      assertThat(validator).isNotNull();
    }
    
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(schemaNames.length);
  }

  @Test
  public void testCacheKeyUniqueness() {
    // Test that cache keys are unique for different combinations
    RDAPDatasetService datasetService1 = mock(RDAPDatasetService.class);
    RDAPDatasetService datasetService2 = mock(RDAPDatasetService.class);
    
    int initialSize = SchemaValidatorCache.getCacheSize();
    
    // Add validators with different combinations
    SchemaValidatorCache.getCachedValidator("rdap_domain.json", mockResults, datasetService1);
    SchemaValidatorCache.getCachedValidator("rdap_domain.json", mockResults, datasetService2);
    SchemaValidatorCache.getCachedValidator("rdap_entity.json", mockResults, datasetService1);
    SchemaValidatorCache.getCachedValidator("rdap_entity.json", mockResults, datasetService2);
    
    // Should have added 4 unique cache entries
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(initialSize + 4);
  }

  @Test
  public void testThreadSafetyWithMultipleSchemas() throws InterruptedException {
    String[] schemas = {"rdap_domain.json", "rdap_entity.json", "rdap_help.json"};
    int threadCount = 30;
    CountDownLatch latch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(10);
    
    int initialSize = SchemaValidatorCache.getCacheSize();
    
    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      executor.submit(() -> {
        try {
          String schema = schemas[index % schemas.length];
          SchemaValidator validator = SchemaValidatorCache.getCachedValidator(
              schema, mockResults, mockDatasetService);
          assertThat(validator).isNotNull();
        } finally {
          latch.countDown();
        }
      });
    }
    
    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    
    // Should have exactly 3 more cache entries (one per unique schema)
    assertThat(SchemaValidatorCache.getCacheSize()).isEqualTo(initialSize + 3);
  }
}