package org.icann.rdapconformance.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public abstract class SchemaValidatorForArrayTest extends SchemaValidatorTest {

  public SchemaValidatorForArrayTest(
      String schemaName,
      String validJson) {
    super(schemaName, validJson);
  }

  @Override
  protected void insertForbiddenKey() {
    JSONObject value = new JSONObject();
    value.put("test", "value");
    jsonObject.getJSONArray(name).getJSONObject(0).put("unknown", List.of(value));
  }

  @Override
  protected void validateSubValidation(String validationName, String keyValue, int errorCode) {
    if (!keyValue.contains(":")) {
      ((JSONArray) jsonObject.get(name)).getJSONObject(0).put(keyValue, 0);
      keyValue = "#/" + name + "/0/" + keyValue + ":0";
    }
    this.validateSubValidation(jsonObject.toString(), errorCode, validationName, keyValue);
  }

  public void keyDoesNotExistInArray(String key, int errorCode) {
    jsonObject.getJSONArray(name).getJSONObject(0).remove(key);
    validateKeyMissing(errorCode, key);
  }

  protected void replaceArrayProperty(String key, Object value) {
    jsonObject.put(name, List.of(jsonObject.getJSONArray(name).getJSONObject(0).put(key,
        value)));
  }

  protected void validateArrayAuthorizedKeys(int error, List<String> authorizedKeys) {
    validateAuthorizedKeys(error, authorizedKeys);
  }

  protected void arrayItemKeyIsNotDateTime(String key, int errorCode) {
    replaceArrayProperty(key, "not a date-time");
    validateIsNotADateTime(errorCode, "#/" + name + "/0/" + key + ":not a date-time");
  }

  protected void arrayItemKeyIsNotString(String key, int errorCode) {
    replaceArrayProperty(key, 0);
    validateIsNotAJsonString(errorCode, "#/" + name + "/0/" + key + ":0");
  }

  protected void linksViolatesLinksValidation(int errorCode) {
    arrayItemKeySubValidation("links", "stdRdapLinksValidation", errorCode);
  }

  protected void arrayItemKeySubValidation(String key, String validationName, int errorCode) {
    replaceArrayProperty(key, 0);
    validateSubValidation(validationName, "#/" + name + "/0/" + key + ":0", errorCode);
  }
}