package org.icann.rdapconformance.validator.util;

import com.github.jknack.handlebars.internal.text.WordUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITestResult;

public class FixturesGenerator {

  /**
   * To be called in a
   *
   * @AfterMethod public void tearDown(ITestResult result) method in case one wants to generate file
   * fixtures based on unit tests.
   */
  public static void generate(ITestResult result,
      JSONObject jsonObject, SchemaValidator schemaValidator)
      throws IOException {
    String objectName = result.getInstanceName()
        .replace("SchemaValidator", "")
        .replace("Test", "")
        .replace("org.icann.rdapconformance.validator.schemavalidator.", "");
    objectName = WordUtils.uncapitalize(objectName);

    JSONObject domain = new JSONObject(
        SchemaValidatorTest.getResource("/validators/domain/valid.json"));
    JSONObject error = new JSONObject(
        SchemaValidatorTest.getResource("/validators/error/valid.json"));
    JSONObject help = new JSONObject(
        SchemaValidatorTest.getResource("/validators/help/valid.json"));
    JSONObject nameserver = new JSONObject(
        SchemaValidatorTest.getResource("/validators/nameserver/valid.json"));
    switch (objectName) {
      case "dsData":
        domain.getJSONObject("secureDNS").put("dsData", jsonObject.get(
            "dsData"));
        jsonObject = domain;
        break;
      case "entity":
        jsonObject = domain.put("entities", List.of(jsonObject));
        break;
      case "entities":
        jsonObject = domain.put("entities", jsonObject.get("entities"));
        break;
      case "events":
        jsonObject = domain.put("events", jsonObject.get("events"));
        break;
      case "lang":
        jsonObject = domain.put("lang", jsonObject.get("lang"));
        break;
      case "links":
        jsonObject = domain.put("links", jsonObject.get("links"));
        break;
      case "notice":
        jsonObject = domain.put("notices", jsonObject.get("notices"));
        break;
      case "rdapConformance":
        jsonObject = domain.put("rdapConformance", jsonObject.get("rdapConformance"));
        break;
      case "asEventActor":
        domain.getJSONArray("entities").getJSONObject(0).put("asEventActor",
            jsonObject.get("asEventActor"));
        jsonObject = domain;
        break;
      case "errorResponseDescription":
        jsonObject = error.put("description", jsonObject.get("description"));
        break;
      case "ipAddress":
        // TODO (ipAddress vs ipAdresses)
        return;
      case "ipv4":
        jsonObject = domain.put("port43", jsonObject.get("ipv4"));
        break;
      case "ipv6":
        jsonObject = domain.put("port43", jsonObject.get("ipv6"));
        break;
      case "keyData":
        domain.getJSONObject("secureDNS").put("keyData", jsonObject.get(
            "keyData"));
        jsonObject = domain;
        break;
      case "ldhName":
      case "unicodeName":
        JSONArray variants = new JSONArray();
        JSONObject variant = new JSONObject();
        variant.put("variantName", jsonObject);
        variants.put(variant);
        jsonObject = domain.put("variants", variants);
        break;
    }

    String root = "/home/gblanchet/Documents/viagenie/RDAP/fixtures/";
    File file = new File(root + result.getInstanceName());
    file.mkdirs();
    Files.write(Paths.get(file.getAbsolutePath() + "/" + result.getMethod().getMethodName()),
        jsonObject.toString(1).getBytes());
  }
}