package org.icann.rdapconformance.validator.schema;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.testng.annotations.Test;

public class SchemaNodeGroupOkTest {

  SchemaNode schemaNode = SchemaNode.create(null, SchemaValidator.getSchema("parent.json",
      "schema/groupOk/",
      getClass().getClassLoader(), new RDAPDatasetServiceMock()));

  @Test
  public void testFindAllValuesOfKey() {
    assertThat(schemaNode.findAllValuesOf("validationName")).contains(
        "childValidation",
        "childItemValidation",
        "childOfChildValidation"
    );
  }
}