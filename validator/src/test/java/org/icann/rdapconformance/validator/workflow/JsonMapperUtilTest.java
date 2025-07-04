package org.icann.rdapconformance.validator.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.testng.annotations.Test;

public class JsonMapperUtilTest {

  @Test
  public void testGetObjectMapperReturnsSameInstance() {
    ObjectMapper mapper1 = JsonMapperUtil.getSharedMapper();
    ObjectMapper mapper2 = JsonMapperUtil.getSharedMapper();
    
    assertThat(mapper1).isSameAs(mapper2);
  }

  @Test
  public void testObjectMapperIsNotNull() {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    assertThat(mapper).isNotNull();
  }

  @Test
  public void testObjectMapperCanSerializeAndDeserialize() throws JsonProcessingException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    
    // Test with a simple object
    TestObject original = new TestObject("test", 42, true);
    String json = mapper.writeValueAsString(original);
    TestObject deserialized = mapper.readValue(json, TestObject.class);
    
    assertThat(deserialized.getName()).isEqualTo(original.getName());
    assertThat(deserialized.getValue()).isEqualTo(original.getValue());
    assertThat(deserialized.isActive()).isEqualTo(original.isActive());
  }

  @Test
  public void testObjectMapperHandlesComplexTypes() throws JsonProcessingException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    
    // Test with collections and nested objects
    Map<String, Object> complexObject = Map.of(
        "string", "value",
        "number", 123,
        "boolean", true,
        "array", List.of(1, 2, 3),
        "nested", Map.of("key", "value")
    );
    
    String json = mapper.writeValueAsString(complexObject);
    Map<String, Object> deserialized = mapper.readValue(json, Map.class);
    
    assertThat(deserialized).containsAllEntriesOf(complexObject);
  }

  @Test
  public void testConcurrentAccessReturnsSameInstance() throws InterruptedException {
    int threadCount = 20;
    CountDownLatch latch = new CountDownLatch(threadCount);
    Set<ObjectMapper> mappers = new HashSet<>();
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    
    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
          synchronized (mappers) {
            mappers.add(mapper);
          }
        } finally {
          latch.countDown();
        }
      });
    }
    
    latch.await(5, TimeUnit.SECONDS);
    executor.shutdown();
    
    // All threads should get the same ObjectMapper instance
    assertThat(mappers).hasSize(1);
  }

  @Test
  public void testObjectMapperIsThreadSafe() throws InterruptedException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    int threadCount = 10;
    int operationsPerThread = 100;
    CountDownLatch latch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger errorCount = new AtomicInteger(0);
    
    for (int i = 0; i < threadCount; i++) {
      final int threadId = i;
      executor.submit(() -> {
        try {
          for (int j = 0; j < operationsPerThread; j++) {
            TestObject obj = new TestObject("thread" + threadId + "_op" + j, threadId * 1000 + j, true);
            String json = mapper.writeValueAsString(obj);
            TestObject result = mapper.readValue(json, TestObject.class);
            
            if (obj.getName().equals(result.getName()) && 
                obj.getValue() == result.getValue() && 
                obj.isActive() == result.isActive()) {
              successCount.incrementAndGet();
            } else {
              errorCount.incrementAndGet();
            }
          }
        } catch (Exception e) {
          errorCount.incrementAndGet();
        } finally {
          latch.countDown();
        }
      });
    }
    
    latch.await(10, TimeUnit.SECONDS);
    executor.shutdown();
    
    // All operations should succeed
    assertThat(successCount.get()).isEqualTo(threadCount * operationsPerThread);
    assertThat(errorCount.get()).isEqualTo(0);
  }

  @Test
  public void testObjectMapperHandlesNullValues() throws JsonProcessingException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    
    TestObject objWithNull = new TestObject(null, 0, false);
    String json = mapper.writeValueAsString(objWithNull);
    TestObject result = mapper.readValue(json, TestObject.class);
    
    assertThat(result.getName()).isNull();
    assertThat(result.getValue()).isEqualTo(0);
    assertThat(result.isActive()).isFalse();
  }

  @Test
  public void testObjectMapperHandlesEmptyObjects() throws JsonProcessingException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    
    String emptyJson = "{}";
    ObjectNode emptyNode = mapper.readValue(emptyJson, ObjectNode.class);
    
    assertThat(emptyNode).isNotNull();
    assertThat(emptyNode.size()).isEqualTo(0);
  }

  @Test
  public void testObjectMapperHandlesArrays() throws JsonProcessingException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    
    List<String> originalList = List.of("one", "two", "three");
    String json = mapper.writeValueAsString(originalList);
    List<String> resultList = mapper.readValue(json, List.class);
    
    assertThat(resultList).containsExactlyElementsOf(originalList);
  }

  @Test
  public void testObjectMapperPerformance() throws JsonProcessingException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    List<TestObject> objects = new ArrayList<>();
    
    // Create test data
    for (int i = 0; i < 100; i++) {
      objects.add(new TestObject("object" + i, i, i % 2 == 0));
    }
    
    // Measure serialization performance
    long startTime = System.currentTimeMillis();
    for (TestObject obj : objects) {
      mapper.writeValueAsString(obj);
    }
    long serializationTime = System.currentTimeMillis() - startTime;
    
    // Measure deserialization performance
    List<String> jsonStrings = new ArrayList<>();
    for (TestObject obj : objects) {
      jsonStrings.add(mapper.writeValueAsString(obj));
    }
    
    startTime = System.currentTimeMillis();
    for (String json : jsonStrings) {
      mapper.readValue(json, TestObject.class);
    }
    long deserializationTime = System.currentTimeMillis() - startTime;
    
    // Just verify operations completed successfully
    // Actual performance will vary by machine
    assertThat(serializationTime).isGreaterThanOrEqualTo(0);
    assertThat(deserializationTime).isGreaterThanOrEqualTo(0);
  }

  @Test
  public void testObjectMapperConfigurationIsConsistent() throws JsonProcessingException {
    ObjectMapper mapper = JsonMapperUtil.getSharedMapper();
    
    // Test that configuration remains consistent across multiple calls
    for (int i = 0; i < 10; i++) {
      ObjectMapper currentMapper = JsonMapperUtil.getSharedMapper();
      assertThat(currentMapper).isSameAs(mapper);
      
      // Verify it can still serialize/deserialize
      TestObject obj = new TestObject("test" + i, i, true);
      String json = currentMapper.writeValueAsString(obj);
      TestObject result = currentMapper.readValue(json, TestObject.class);
      assertThat(result.getName()).isEqualTo(obj.getName());
    }
  }

  // Test helper class
  static class TestObject {
    private String name;
    private int value;
    private boolean active;
    
    public TestObject() {
      // Default constructor for Jackson
    }
    
    public TestObject(String name, int value, boolean active) {
      this.name = name;
      this.value = value;
      this.active = active;
    }
    
    public String getName() {
      return name;
    }
    
    public void setName(String name) {
      this.name = name;
    }
    
    public int getValue() {
      return value;
    }
    
    public void setValue(int value) {
      this.value = value;
    }
    
    public boolean isActive() {
      return active;
    }
    
    public void setActive(boolean active) {
      this.active = active;
    }
  }
}