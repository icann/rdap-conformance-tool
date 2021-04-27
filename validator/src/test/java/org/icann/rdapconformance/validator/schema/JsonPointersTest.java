package org.icann.rdapconformance.validator.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.testng.annotations.Test;

public class JsonPointersTest {

  @Test
  public void testGetOnlyTopMosts() {
    JsonPointers jsonPointers = new JsonPointers(Set.of("#/myObject", "#/aParent/of/myObject"));
    assertThat(jsonPointers.getOnlyTopMosts()).contains("#/myObject");
    assertThat(jsonPointers.getAll()).contains("#/myObject", "#/aParent/of/myObject");
  }

  @Test
  public void testGetParentOfTopMosts() {
    JsonPointers jsonPointers = new JsonPointers(Set.of("#/myArray1/0", "#/aParent/of/myArray2/1"));
    assertThat(jsonPointers.getParentOfTopMosts()).contains("#/myArray1");
  }
}