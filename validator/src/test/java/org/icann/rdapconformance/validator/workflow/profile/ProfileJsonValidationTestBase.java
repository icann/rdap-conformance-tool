package org.icann.rdapconformance.validator.workflow.profile;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import net.minidev.json.JSONArray;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.util.FixturesGenerator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.json.JSONObject;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class ProfileJsonValidationTestBase extends ProfileValidationTestBase {

  protected final String validJsonResourcePath;
  protected final String testGroupName;
  protected JSONObject jsonObject;
  protected String rdapContent;
  protected RDAPDatasetService datasets;
  protected String validationName;

  public ProfileJsonValidationTestBase(
      String validJsonResourcePath,
      String testGroupName) {
    this.validJsonResourcePath = validJsonResourcePath;
    this.testGroupName = testGroupName;
  }

  @Override
  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    datasets = new RDAPDatasetServiceMock();
    datasets.download(true);
    rdapContent = getResource(validJsonResourcePath);
    jsonObject = new JSONObject(rdapContent);
  }

  public <T> T getValue(String jpath) {
    return JsonPath
        .read(jsonObject.toString(), jpath);
  }

  public void putValue(String jpath, String key, Object value) {
    rdapContent = JsonPath
        .parse(jsonObject.toString())
        .put(jpath, key, value)
        .jsonString();
    jsonObject = new JSONObject(rdapContent);
  }

  public void addValue(String jpath, Object value) {
    JSONArray array = getValue(jpath);
    array.add(value);
    rdapContent = JsonPath
        .parse(jsonObject.toString())
        .set(jpath, array)
        .jsonString();
    jsonObject = new JSONObject(rdapContent);
  }

  public void replaceValue(String jpath, Object value) {
    rdapContent = JsonPath
        .parse(jsonObject.toString())
        .set(jpath, value)
        .jsonString();
    jsonObject = new JSONObject(rdapContent);
  }

  public void removeKey(String jpath) {
    rdapContent = JsonPath
        .parse(jsonObject.toString())
        .delete(jpath)
        .jsonString();
    jsonObject = new JSONObject(rdapContent);
  }

  @AfterMethod
  public void tearDown(ITestResult testResult) throws IOException {
    // Only call generate if jsonObject is not null
    if (jsonObject != null) {
      FixturesGenerator.generate(testResult, jsonObject);
    }
  }
}
