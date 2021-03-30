package org.icann.rdapconformance.validator.exception.parser;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import org.testng.annotations.Test;

public class ExceptionParserTest {
  @Test
  public void testGetParentSchema() throws URISyntaxException {
    String parentSchemaName = ExceptionParser.getParentSchemaName("#/notices/0/links");
    assertThat(parentSchemaName).isEqualTo("notices");
  }
}