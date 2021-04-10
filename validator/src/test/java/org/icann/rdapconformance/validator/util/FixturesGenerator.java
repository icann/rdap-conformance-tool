package org.icann.rdapconformance.validator.util;

import com.github.jknack.handlebars.internal.text.WordUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.SchemaValidatorTest;
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
        .replace("org.icann.rdapconformance.validator.", "");
    objectName = WordUtils.uncapitalize(objectName);

    JSONObject domain = new JSONObject(SchemaValidatorTest.getResource("/validators/domain/valid.json"));
    switch (objectName) {
      case "entity":
        jsonObject = domain.put("entities", new JSONArray(List.of(jsonObject)));
        break;
      case "events":
        jsonObject = domain.put("events", jsonObject.getJSONArray("events"));
        break;
      case "lang":
        jsonObject = domain.put("lang", jsonObject.get("lang"));
        break;
      case "links":
        jsonObject = domain.put("links", jsonObject.getJSONArray("links"));
        break;
      case "notice":
        jsonObject = domain.put("notices", jsonObject.getJSONArray("notices"));
        break;
      case "rdapConformance":
        jsonObject = domain.put("rdapConformance", jsonObject.getJSONArray("rdapConformance"));
        break;
    }

    if (!schemaValidator.validate(jsonObject.toString())) {
      String root = "/home/gblanchet/Documents/viagenie/RDAP/fixtures/";
      File file = new File(root + result.getInstanceName());
      file.mkdirs();
      Files.write(Paths.get(file.getAbsolutePath() + "/" + result.getMethod().getMethodName()),
          jsonObject.toString(1).getBytes());
    }
  }
}
