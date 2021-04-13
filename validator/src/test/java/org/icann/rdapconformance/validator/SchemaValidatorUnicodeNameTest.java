package org.icann.rdapconformance.validator;

import java.io.IOException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SchemaValidatorUnicodeNameTest extends SchemaValidatorIdnaTest {

  public SchemaValidatorUnicodeNameTest() {
    super("viagénie.ca");
  }

  @BeforeMethod
  @Override
  public void setUp() throws IOException {
    super.setUp();
    name = "unicodeName";
  }

  /**
   * 7.2.12.1.
   */
  @Test
  public void lengthExceeding63Characters() {
    lengthExceeding63Characters(-11600);
  }

  /**
   * 7.2.12.2.
   */
  @Test
  public void totalLengthExceeding253Characters() {
    totalLengthExceeding253Characters(-11601);
  }

  /**
   * 7.2.12.3.
   */
  @Test
  public void domainWithLessThan2Labels() {
    domainWithLessThan2Labels(-11602);
  }

  /**
   * 7.2.12.4.
   */
  @Test
  public void ulabelInvalid() {
    labelInvalid(-11603);
  }
}
