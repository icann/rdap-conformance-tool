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

  @Test
  public void testGetCachedJson_ValidObject() {
    String objectContent = "{\"test\":true}";
    
    JsonCacheUtil.CachedJsonResult result = JsonCacheUtil.getCachedJson(objectContent);
    
    assertThat(result.isValid()).isTrue();
    assertThat(result.isJsonObject()).isTrue();
    assertThat(result.isJsonArray()).isFalse();
    assertThat(result.getJsonObject()).isNotNull();
    assertThat(result.getJsonArray()).isNull();
    assertThat(result.getJsonObject().getBoolean("test")).isTrue();
  }

  @Test
  public void testGetCachedJson_ValidArray() {
    String arrayContent = "[1,2,3]";
    
    JsonCacheUtil.CachedJsonResult result = JsonCacheUtil.getCachedJson(arrayContent);
    
    assertThat(result.isValid()).isTrue();
    assertThat(result.isJsonObject()).isFalse();
    assertThat(result.isJsonArray()).isTrue();
    assertThat(result.getJsonObject()).isNull();
    assertThat(result.getJsonArray()).isNotNull();
    assertThat(result.getJsonArray().length()).isEqualTo(3);
  }

  @Test
  public void testGetCachedJson_InvalidContent() {
    String invalidContent = "not json at all";
    
    JsonCacheUtil.CachedJsonResult result = JsonCacheUtil.getCachedJson(invalidContent);
    
    assertThat(result.isValid()).isFalse();
    assertThat(result.isJsonObject()).isFalse();
    assertThat(result.isJsonArray()).isFalse();
    assertThat(result.getJsonObject()).isNull();
    assertThat(result.getJsonArray()).isNull();
  }

  @Test
  public void testGetCachedJson_NullContent() {
    JsonCacheUtil.CachedJsonResult result = JsonCacheUtil.getCachedJson(null);
    
    assertThat(result.isValid()).isFalse();
    assertThat(result.isJsonObject()).isFalse();
    assertThat(result.isJsonArray()).isFalse();
    assertThat(result.getJsonObject()).isNull();
    assertThat(result.getJsonArray()).isNull();
  }

  @Test
  public void testGetCachedJson_EmptyContent() {
    JsonCacheUtil.CachedJsonResult result = JsonCacheUtil.getCachedJson("");
    
    assertThat(result.isValid()).isFalse();
    assertThat(result.isJsonObject()).isFalse();
    assertThat(result.isJsonArray()).isFalse();
    assertThat(result.getJsonObject()).isNull();
    assertThat(result.getJsonArray()).isNull();
  }

  @Test
  public void testCachedJsonResult_Constructor() {
    JSONObject jsonObject = new JSONObject("{\"test\":true}");
    JSONArray jsonArray = new JSONArray("[1,2,3]");
    
    JsonCacheUtil.CachedJsonResult objectResult = new JsonCacheUtil.CachedJsonResult(jsonObject, null, true);
    JsonCacheUtil.CachedJsonResult arrayResult = new JsonCacheUtil.CachedJsonResult(null, jsonArray, true);
    JsonCacheUtil.CachedJsonResult invalidResult = new JsonCacheUtil.CachedJsonResult(null, null, false);
    
    assertThat(objectResult.getJsonObject()).isSameAs(jsonObject);
    assertThat(objectResult.getJsonArray()).isNull();
    assertThat(objectResult.isValid()).isTrue();
    assertThat(objectResult.isJsonObject()).isTrue();
    assertThat(objectResult.isJsonArray()).isFalse();
    
    assertThat(arrayResult.getJsonObject()).isNull();
    assertThat(arrayResult.getJsonArray()).isSameAs(jsonArray);
    assertThat(arrayResult.isValid()).isTrue();
    assertThat(arrayResult.isJsonObject()).isFalse();
    assertThat(arrayResult.isJsonArray()).isTrue();
    
    assertThat(invalidResult.getJsonObject()).isNull();
    assertThat(invalidResult.getJsonArray()).isNull();
    assertThat(invalidResult.isValid()).isFalse();
    assertThat(invalidResult.isJsonObject()).isFalse();
    assertThat(invalidResult.isJsonArray()).isFalse();
  }

  @Test
  public void testCacheSizes() {
    assertThat(JsonCacheUtil.getJsonObjectCacheSize()).isEqualTo(0);
    assertThat(JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(0);
    
    JsonCacheUtil.getCachedJsonObject("{\"test\":1}");
    JsonCacheUtil.getCachedJsonObject("{\"test\":2}");
    JsonCacheUtil.getCachedJsonArray("[1]");
    JsonCacheUtil.getCachedJsonArray("[2]");
    
    assertThat(JsonCacheUtil.getJsonObjectCacheSize()).isEqualTo(2);
    assertThat(JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(2);
  }

  @Test
  public void testClearSpecificCaches() {
    JsonCacheUtil.getCachedJsonObject("{\"test\":1}");
    JsonCacheUtil.getCachedJsonArray("[1]");
    
    assertThat(JsonCacheUtil.getJsonObjectCacheSize()).isEqualTo(1);
    assertThat(JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(1);
    
    JsonCacheUtil.clearJsonObjectCache();
    
    assertThat(JsonCacheUtil.getJsonObjectCacheSize()).isEqualTo(0);
    assertThat(JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(1);
    
    JsonCacheUtil.clearJsonArrayCache();
    
    assertThat(JsonCacheUtil.getJsonArrayCacheSize()).isEqualTo(0);
  }

  @Test
  public void testEmptyStringInput() {
    assertThatThrownBy(() -> JsonCacheUtil.getCachedJsonObject(""))
        .isInstanceOf(JSONException.class)
        .hasMessage("Content cannot be null or empty");
        
    assertThatThrownBy(() -> JsonCacheUtil.getCachedJsonArray(""))
        .isInstanceOf(JSONException.class)
        .hasMessage("Content cannot be null or empty");
  }

  @Test
  public void testCacheEviction() {
    // Test cache eviction by filling beyond max size (100)
    for (int i = 0; i < 110; i++) {
      String content = "{\"key" + i + "\":\"value" + i + "\"}";
      JsonCacheUtil.getCachedJsonObject(content);
    }
    
    // Cache should have performed eviction
    assertThat(JsonCacheUtil.getJsonObjectCacheSize()).isLessThan(110);
  }
}