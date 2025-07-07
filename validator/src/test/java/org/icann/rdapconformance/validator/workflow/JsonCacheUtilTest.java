package org.icann.rdapconformance.validator.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

public class JsonCacheUtilTest {

  @BeforeMethod
  public void setUp() {
    // Clear cache before each test
    JsonCacheUtil.clearAllCaches();
  }

  @AfterMethod
  public void tearDown() {
    // Clear cache after each test
    JsonCacheUtil.clearAllCaches();
  }

  @Test
  public void testGetCachedJsonObjectReturnsSameInstance() {
    String json = "{\"key\":\"value\",\"number\":123}";
    
    JSONObject obj1 = JsonCacheUtil.getCachedJsonObject(json);
    JSONObject obj2 = JsonCacheUtil.getCachedJsonObject(json);
    
    // Should return the same cached instance
    assertThat(obj1).isSameAs(obj2);
  }

  @Test
  public void testGetCachedJsonObjectHandlesDifferentContent() {
    String json1 = "{\"key1\":\"value1\"}";
    String json2 = "{\"key2\":\"value2\"}";
    
    JSONObject obj1 = JsonCacheUtil.getCachedJsonObject(json1);
    JSONObject obj2 = JsonCacheUtil.getCachedJsonObject(json2);
    
    assertThat(obj1).isNotSameAs(obj2);
    assertThat(obj1.getString("key1")).isEqualTo("value1");
    assertThat(obj2.getString("key2")).isEqualTo("value2");
  }

  @Test
  public void testGetCachedJsonArrayReturnsSameInstance() {
    String json = "[1,2,3,4,5]";
    
    JSONArray arr1 = JsonCacheUtil.getCachedJsonArray(json);
    JSONArray arr2 = JsonCacheUtil.getCachedJsonArray(json);
    
    // Should return the same cached instance
    assertThat(arr1).isSameAs(arr2);
  }

  @Test
  public void testGetCachedJsonArrayHandlesDifferentContent() {
    String json1 = "[1,2,3]";
    String json2 = "[\"a\",\"b\",\"c\"]";
    
    JSONArray arr1 = JsonCacheUtil.getCachedJsonArray(json1);
    JSONArray arr2 = JsonCacheUtil.getCachedJsonArray(json2);
    
    assertThat(arr1).isNotSameAs(arr2);
    assertThat(arr1.length()).isEqualTo(3);
    assertThat(arr2.length()).isEqualTo(3);
    assertThat(arr1.getInt(0)).isEqualTo(1);
    assertThat(arr2.getString(0)).isEqualTo("a");
  }

  @Test
  public void testDuplicateKeysThrowException() {
    String jsonWithDuplicates = "{\"key\":\"value1\",\"key\":\"value2\"}";
    
    // Should throw exception due to duplicate keys
    assertThatThrownBy(() -> JsonCacheUtil.getCachedJsonObject(jsonWithDuplicates))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to parse JSON object");
  }

  @Test
  public void testInvalidJsonThrowsException() {
    String invalidJson = "{invalid json}";
    
    assertThatThrownBy(() -> JsonCacheUtil.getCachedJsonObject(invalidJson))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to parse JSON object");
    
    assertThatThrownBy(() -> JsonCacheUtil.getCachedJsonArray(invalidJson))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Failed to parse JSON array");
  }

  @Test
  public void testNullJsonThrowsException() {
    assertThatThrownBy(() -> JsonCacheUtil.getCachedJsonObject(null))
        .isInstanceOf(JSONException.class);
    
    assertThatThrownBy(() -> JsonCacheUtil.getCachedJsonArray(null))
        .isInstanceOf(JSONException.class);
  }

  @Test
  public void testEmptyJsonObject() {
    String emptyJson = "{}";
    
    JSONObject obj = JsonCacheUtil.getCachedJsonObject(emptyJson);
    assertThat(obj).isNotNull();
    assertThat(obj.length()).isEqualTo(0);
  }

  @Test
  public void testEmptyJsonArray() {
    String emptyJson = "[]";
    
    JSONArray arr = JsonCacheUtil.getCachedJsonArray(emptyJson);
    assertThat(arr).isNotNull();
    assertThat(arr.length()).isEqualTo(0);
  }

  @Test
  public void testClearCache() {
    String json1 = "{\"key\":\"value\"}";
    String json2 = "[1,2,3]";
    
    JSONObject obj1 = JsonCacheUtil.getCachedJsonObject(json1);
    JSONArray arr1 = JsonCacheUtil.getCachedJsonArray(json2);
    
    // Clear the cache
    JsonCacheUtil.clearAllCaches();
    
    // Get new instances
    JSONObject obj2 = JsonCacheUtil.getCachedJsonObject(json1);
    JSONArray arr2 = JsonCacheUtil.getCachedJsonArray(json2);
    
    // Should be different instances after cache clear
    assertThat(obj1).isNotSameAs(obj2);
    assertThat(arr1).isNotSameAs(arr2);
  }

  @Test
  public void testGetCacheSize() {
    assertThat(JsonCacheUtil.getJsonObjectCacheSize() + JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(0);
    
    JsonCacheUtil.getCachedJsonObject("{\"key1\":\"value1\"}");
    assertThat(JsonCacheUtil.getJsonObjectCacheSize() + JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(1);
    
    JsonCacheUtil.getCachedJsonObject("{\"key2\":\"value2\"}");
    assertThat(JsonCacheUtil.getJsonObjectCacheSize() + JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(2);
    
    JsonCacheUtil.getCachedJsonArray("[1,2,3]");
    assertThat(JsonCacheUtil.getJsonObjectCacheSize() + JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(3);
    
    // Same content should not increase cache size
    JsonCacheUtil.getCachedJsonObject("{\"key1\":\"value1\"}");
    assertThat(JsonCacheUtil.getJsonObjectCacheSize() + JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(3);
  }

  @Test
  public void testBasicCacheEviction() {
    // Add a small number of items to test basic eviction behavior
    for (int i = 0; i < 10; i++) {
      JsonCacheUtil.getCachedJsonObject("{\"key" + i + "\":\"value" + i + "\"}");
    }
    
    int cacheSize = JsonCacheUtil.getJsonObjectCacheSize();
    assertThat(cacheSize).isEqualTo(10);
    
    // Add one more
    JsonCacheUtil.getCachedJsonObject("{\"keyNew\":\"valueNew\"}");
    
    // Cache should have grown by 1
    assertThat(JsonCacheUtil.getJsonObjectCacheSize()).isEqualTo(11);
  }

  @Test
  public void testComplexNestedJson() {
    String complexJson = """
        {
          "name": "test",
          "nested": {
            "array": [1, 2, {"inner": "value"}],
            "boolean": true,
            "null": null
          },
          "list": ["a", "b", "c"]
        }
        """;
    
    JSONObject obj1 = JsonCacheUtil.getCachedJsonObject(complexJson);
    JSONObject obj2 = JsonCacheUtil.getCachedJsonObject(complexJson);
    
    assertThat(obj1).isSameAs(obj2);
    assertThat(obj1.getJSONObject("nested").getJSONArray("array").length()).isEqualTo(3);
  }

  @Test
  public void testWhitespaceHandling() {
    String json1 = "{\"key\":\"value\"}";
    String json2 = "{ \"key\" : \"value\" }";
    String json3 = "{\n  \"key\": \"value\"\n}";
    
    JSONObject obj1 = JsonCacheUtil.getCachedJsonObject(json1);
    JSONObject obj2 = JsonCacheUtil.getCachedJsonObject(json2);
    JSONObject obj3 = JsonCacheUtil.getCachedJsonObject(json3);
    
    // Different whitespace means different cache entries
    assertThat(obj1).isNotSameAs(obj2);
    assertThat(obj2).isNotSameAs(obj3);
    assertThat(obj1).isNotSameAs(obj3);
    
    // But they should have the same content
    assertThat(obj1.getString("key")).isEqualTo("value");
    assertThat(obj2.getString("key")).isEqualTo("value");
    assertThat(obj3.getString("key")).isEqualTo("value");
  }
}