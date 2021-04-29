package org.icann.rdapconformance.validator.schema;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonPointers {

  private static Pattern lastArrayIndex = Pattern.compile("^(.*)\\/\\d+$");
  private Set<String> jsonPointers = new HashSet<>();

  public JsonPointers() {
  }

  public JsonPointers(Set<String> jsonPointers) {
    this.jsonPointers = jsonPointers;
  }

  public static String fromJpath(String absoluteJpath) {
    return absoluteJpath
        .replace("$", "#")
        .replace("[", "/")
        .replace("]", "")
        .replace("'", "");
  }

  public Set<String> getAll() {
    return jsonPointers;
  }

  /**
   * Get the only the top most json pointers.
   */
  public Set<String> getOnlyTopMosts() {
    int minElements =
        jsonPointers.stream()
            .map(s -> s.split("/").length)
            .min(Integer::compare)
            .orElse(0);

    return jsonPointers.stream()
        .filter(s -> s.split("/").length == minElements)
        .collect(Collectors.toSet());
  }

  /**
   * In case of an array, get the parent array instead of the elements.
   */
  public Optional<String> getParentOfTopMosts() {
    return getOnlyTopMosts()
        .stream().findFirst()
        .map(jsonPointer -> {
          Matcher matcher = lastArrayIndex.matcher(jsonPointer);
          if (matcher.find()) {
            return matcher.group(1);
          }
          return jsonPointer;
        });

  }
}
