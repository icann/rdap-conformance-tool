package org.icann.rdapconformance.validator.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import com.github.jknack.handlebars.internal.text.WordUtils;
import org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.ITestResult;

public class FixturesGenerator {

  public static void generate(ITestResult result, JSONObject jsonObject)
      throws IOException {
    String objectName = result.getInstanceName()
        .replace("SchemaValidator", "")
        .replace("Test", "")
        .replace("org.icann.rdapconformance.validator.schemavalidator.", "")
        .replace("org.icann.rdapconformance.validator.workflow.", "");
    objectName = WordUtils.uncapitalize(objectName);

    JSONObject domain = new JSONObject(
        SchemaValidatorTest.getResource("/validators/domain/valid.json"));
    JSONObject error = new JSONObject(
        SchemaValidatorTest.getResource("/validators/error/valid.json"));
    JSONObject help = new JSONObject(
        SchemaValidatorTest.getResource("/validators/help/valid.json"));
    JSONObject nameserver = new JSONObject(
        SchemaValidatorTest.getResource("/validators/nameserver/valid.json"));
    JSONObject variant = new JSONObject();
    switch (objectName) {
      case "asEventActor":
        domain.getJSONArray("entities").getJSONObject(0).put("asEventActor",
            jsonObject.get("asEventActor"));
        jsonObject = domain;
        break;
      case "domain":
        // already topmost object
        break;
      case "dsData":
        domain.getJSONObject("secureDNS").put("dsData", jsonObject.get(
            "dsData"));
        jsonObject = domain;
        break;
      case "entities":
        jsonObject = domain.put("entities", jsonObject.get("entities"));
        break;
      case "entity":
        jsonObject = domain.put("entities", List.of(jsonObject));
        break;
      case "errorResponseDescription":
        jsonObject = error.put("description", jsonObject.get("description"));
        break;
      case "errorResponse":
        // already topmost object
        break;
      case "events":
        jsonObject = domain.put("events", jsonObject.get("events"));
        break;
      case "help":
        // already topmost object
        break;
      case "ipAddress":
        // strangely, these validations from specifications cannot happen in a topmost object,
        // to be clarified later...
        if (Set.of("v4Orv6NotBoth", "invalid", "unauthorizedKey").contains(result.getMethod().getMethodName())) {
          return;
        }

        JSONObject ipAddress = jsonObject.getJSONObject("ipAddress");

        if (ipAddress.has("v4")) {
          jsonObject = domain.put("port43", ipAddress.get("v4"));
        } else if (ipAddress.has("v6")) {
          jsonObject = domain.put("port43", ipAddress.get("v6"));
        }
        break;
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
      case "lang":
        jsonObject = domain.put("lang", jsonObject.get("lang"));
        break;
      case "ldhName":
        ldhOrUnicodeName(domain, jsonObject);
        break;
      case "links":
        jsonObject = domain.put("links", jsonObject.get("links"));
        break;
      case "nameserversSearch":
        // already topmost object
        break;
      case "nameserver":
        jsonObject = domain.put("nameservers", List.of(jsonObject));
        break;
      case "notices":
        jsonObject = domain.put("notices", jsonObject.get("notices"));
        break;
      case "port43":
        jsonObject = domain.put("port43", jsonObject.get("port43"));
        break;
      case "publicIds":
        jsonObject = domain.put("publicIds", jsonObject.get("publicIds"));
        break;
      case "rdapConformance":
        jsonObject = domain.put("rdapConformance", jsonObject.get("rdapConformance"));
        break;
      case "roles":
        domain.getJSONArray("entities").getJSONObject(0).put("roles", jsonObject.get("roles"));
        jsonObject = domain;
        break;
      case "secureDns":
        // already topmost object
        break;
      case "status":
        jsonObject = domain.put("status", jsonObject.get("status"));
        break;
      case "unicodeName":
        ldhOrUnicodeName(domain, jsonObject);
        break;
      case "variantNames":
        variant.put("variantNames", jsonObject);
        jsonObject = domain.put("variants", List.of(variant));
        break;
      case "variantRelation":
        variant.put("variantRelation", jsonObject);
        jsonObject = domain.put("variants", List.of(variant));
        break;
      case "variants":
        jsonObject = domain.put("variants", jsonObject);
        break;
      case "vcardArrayInDomain":
        // already topmost object
        break;
      case "webUri":
        domain.getJSONArray("links").getJSONObject(0).put("value", jsonObject.get("webUri"));
        jsonObject = domain;
        break;
    }

    String root = "../fixtures/";
    File file = new File(root + objectName);
    file.mkdirs();
    Files.write(
        Paths.get(file.getAbsolutePath() + "/" + result.getMethod().getMethodName() + ".json"),
        jsonObject.toString(1).getBytes());
  }

  private static void ldhOrUnicodeName(JSONObject domain, JSONObject jsonObject) {
    JSONArray variants = new JSONArray();
    JSONObject variant = new JSONObject();
    variant.put("variantName", jsonObject);
    variants.put(variant);
    jsonObject = domain.put("variants", variants);
  }
}