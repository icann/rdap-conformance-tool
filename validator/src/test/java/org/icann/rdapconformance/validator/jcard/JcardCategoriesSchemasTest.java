package org.icann.rdapconformance.validator.jcard;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

public class JcardCategoriesSchemasTest {

  JcardCategoriesSchemas jcardSchemas = new JcardCategoriesSchemas();

  @Ignore
  @Test
  public void writeCategories() throws IOException {
    JSONArray jsonSchema = new JSONObject(
        new JSONTokener(
            Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream("categories.json"))))
        .getJSONArray("categories");

    JSONObject newSchema = new JSONObject();
    String testCodes = "";
    for (Object element : jsonSchema) {
      String category = null;
      JSONObject firstSubElement =
          ((JSONObject) element).getJSONArray("items").getJSONObject(0);
      if (firstSubElement.has("const")) {
        category = firstSubElement.getString("const");
      } else if (firstSubElement.has("pattern")) {
        category = firstSubElement.getString("pattern");
      }

      if (newSchema.has(category)) {
        if (newSchema.getJSONObject(category).has("oneOf")) {
          newSchema.getJSONObject(category).getJSONArray("oneOf").put(element);
        } else {
          JSONObject oneOf = new JSONObject();
          oneOf.put("oneOf", List.of(newSchema.get(category), element));
          newSchema.put(category, oneOf);
        }
      } else {
        newSchema.put(category, element);
      }



      testCodes += "\n"
          + "  @Test\n"
          + "  public void "+category+"Category() {\n"
          + "    testLoadingCategory(\""+category+"\");\n"
          + "  }\n";
    }

    File file = new File("/tmp/jcard_categories.json");
    Files.write(Paths.get(file.getAbsolutePath()),
        newSchema.toString(1).getBytes());

    File fileCode = new File("/tmp/tests.java");
    Files.write(Paths.get(fileCode.getAbsolutePath()),
        testCodes.getBytes());
  }

  @Test
  public void adrCategory() {
    testLoadingCategory("adr");
  }

  @Test
  public void fnCategory() {
    testLoadingCategory("fn");
  }

  @Test
  public void nCategory() {
    testLoadingCategory("n");
  }

  @Test
  public void nicknameCategory() {
    testLoadingCategory("nickname");
  }

  @Test
  public void photoCategory() {
    testLoadingCategory("photo");
  }


  @Test
  public void bdayCategory() {
    testLoadingCategory("bday");
  }

  @Test
  public void anniversaryCategory() {
    testLoadingCategory("anniversary");
  }

  @Test
  public void genderCategory() {
    testLoadingCategory("gender");
  }

  @Test
  public void telCategory() {
    testLoadingCategory("tel");
  }

  @Test
  public void emailCategory() {
    testLoadingCategory("email");
  }

  @Test
  public void imppCategory() {
    testLoadingCategory("impp");
  }

  @Test
  public void langCategory() {
    testLoadingCategory("lang");
  }

  @Test
  public void kindCategory() {
    testLoadingCategory("kind");
  }

  @Test
  public void tzCategory() {
    testLoadingCategory("tz");
  }

  @Test
  public void geoCategory() {
    testLoadingCategory("geo");
  }

  @Test
  public void sourceCategory() {
    testLoadingCategory("source");
  }

  @Test
  public void keyCategory() {
    testLoadingCategory("key");
  }

  @Test
  public void titleCategory() {
    testLoadingCategory("title");
  }

  @Test
  public void roleCategory() {
    testLoadingCategory("role");
  }

  @Test
  public void logoCategory() {
    testLoadingCategory("logo");
  }

  @Test
  public void orgCategory() {
    testLoadingCategory("org");
  }

  @Test
  public void urlCategory() {
    testLoadingCategory("url");
  }

  @Test
  public void uidCategory() {
    testLoadingCategory("uid");
  }

  @Test
  public void noteCategory() {
    testLoadingCategory("note");
  }

  @Test
  public void prodidCategory() {
    testLoadingCategory("prodid");
  }

  @Test
  public void memberCategory() {
    testLoadingCategory("member");
  }

  @Test
  public void relatedCategory() {
    testLoadingCategory("related");
  }

  @Test
  public void revCategory() {
    testLoadingCategory("rev");
  }

  @Test
  public void soundCategory() {
    testLoadingCategory("sound");
  }

  @Test
  public void fburlCategory() {
    testLoadingCategory("fburl");
  }

  @Test
  public void categoriesCategory() {
    testLoadingCategory("categories");
  }

  @Test
  public void caladruriCategory() {
    testLoadingCategory("caladruri");
  }

  @Test
  public void caluriCategory() {
    testLoadingCategory("caluri");
  }

  @Test
  public void clientpidmapCategory() {
    testLoadingCategory("clientpidmap");
  }

  @Test
  public void expertiseCategory() {
    testLoadingCategory("expertise");
  }

  @Test
  public void hobbyCategory() {
    testLoadingCategory("hobby");
  }

  @Test
  public void interestCategory() {
    testLoadingCategory("interest");
  }

  @Test
  public void orgdirectoryCategory() {
    testLoadingCategory("org-directory");
  }

  @Test
  public void birthplaceCategory() {
    testLoadingCategory("birthplace");
  }


  @Test
  public void deathplaceCategory() {
    testLoadingCategory("deathplace");
  }

  @Test
  public void deathdateCategory() {
    testLoadingCategory("deathdate");
  }

  @Test
  public void xPatternCategory() {
    assertThat(jcardSchemas.getCategory("x-[a-z0-9-]*").toString())
        .contains("\"pattern\":\"x-[a-z0-9-]*\"");
  }

  private void testLoadingCategory(String category) {
    assertThat(jcardSchemas.getCategory(category).toString())
        .contains("\"const\":\"" + category + "\"");
  }
}