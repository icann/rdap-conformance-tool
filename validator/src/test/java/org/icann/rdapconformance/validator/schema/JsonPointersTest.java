package org.icann.rdapconformance.validator.schema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.apache.commons.collections4.set.ListOrderedSet;
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

  @Test
  public void testGetParentOfTopMostsWithMultipleEntries() {
    ListOrderedSet listOrderedSet = new ListOrderedSet();
    listOrderedSet.add("#/myArray1/1");
    listOrderedSet.add("#/myArray1/2");
    JsonPointers jsonPointers = new JsonPointers(listOrderedSet);
    assertThat(jsonPointers.getParentOfTopMosts()).contains("#/myArray1");
  }

  @Test
  public void testGetParentOfTopMostsWithoutArray() {
    JsonPointers jsonPointers = new JsonPointers(Set.of("#/myObject"));
    assertThat(jsonPointers.getParentOfTopMosts()).contains("#/myObject");
  }

  @Test
  public void fromJpath() {
    String jsonPointer = JsonPointers.fromJpath("$['entities'][0]['entities'][0]['vcardArray']");
    assertThat(jsonPointer).isEqualTo("#/entities/0/entities/0/vcardArray");
  }
}