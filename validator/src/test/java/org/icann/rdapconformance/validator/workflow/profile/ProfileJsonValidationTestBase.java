package org.icann.rdapconformance.validator.workflow.profile;

import static org.icann.rdapconformance.validator.schemavalidator.SchemaValidatorTest.getResource;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.json.JSONObject;
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

  public <T> T  getValue(String jpath) {
    return JsonPath
        .read(jsonObject.toString(), jpath);
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
}
