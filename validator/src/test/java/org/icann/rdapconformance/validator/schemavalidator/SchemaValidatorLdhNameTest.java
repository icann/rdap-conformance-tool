package org.icann.rdapconformance.validator.schemavalidator;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorLdhNameTest extends SchemaValidatorIdnaTest {

  public SchemaValidatorLdhNameTest() {
    super("viagenie.ca");
    validationName = "stdRdapLdhNameValidation";
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "ldhName";
  }


  /**
   * 7.2.13.1.
   */
  @Test
  public void lengthExceeding63Characters() {
    lengthExceeding63Characters(-11700);
  }

  /**
   * 7.2.13.2.
   */
  @Test
  public void totalLengthExceeding253Characters() {
    totalLengthExceeding253Characters(-11701);
  }

  /**
   * 7.2.13.3.
   */
  @Test
  public void domainWithLessThan2Labels() {
    domainWithLessThan2Labels(-11702);
  }

  /**
   * 7.2.13.4.
   */
  @Test
  public void alabelInvalid() {
    labelInvalid(-11703);
  }
}
