package org.icann.rdapconformance.validator.workflow;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.icann.rdapconformance.validator.workflow.DomainCaseFoldingValidation.CaseFoldingJsonComparator;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit suite for the -10403 comparator (domain case folding).
 *
 * Rules: link "value" and "href" ignored; link "rel"/"type" compared case-insensitively;
 * property order, array order and "last update of RDAP database" ignored;
 * any other real difference must be detected.
 */
public class CaseFoldingJsonComparatorTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private final CaseFoldingJsonComparator comparator = new CaseFoldingJsonComparator();

  private static final String BASE = "{"
      + "\"objectClassName\":\"domain\",\"handle\":\"H-1\",\"ldhName\":\"orbot.com\","
      + "\"status\":[\"client delete prohibited\",\"client transfer prohibited\"],"
      + "\"links\":[{\"value\":\"%1$s\",\"rel\":\"%2$s\",\"href\":\"%3$s\",\"type\":\"%4$s\"}],"
      + "\"events\":[{\"eventAction\":\"registration\",\"eventDate\":\"1997-09-15T04:00:00Z\"},"
      + "{\"eventAction\":\"last update of RDAP database\",\"eventDate\":\"%5$s\"}],"
      + "\"nameservers\":[{\"objectClassName\":\"nameserver\",\"ldhName\":\"NS1.orbot.com\"},"
      + "{\"objectClassName\":\"nameserver\",\"ldhName\":\"NS2.orbot.com\"}],"
      + "\"notices\":[{\"title\":\"T\",\"links\":[{\"value\":\"%1$s\",\"rel\":\"%2$s\",\"href\":\"%3$s\",\"type\":\"%4$s\"}]}],"
      + "\"entities\":[{\"objectClassName\":\"entity\",\"handle\":\"292\",\"roles\":[\"registrar\"],"
      + "\"links\":[{\"value\":\"%1$s\",\"rel\":\"%2$s\",\"href\":\"%3$s\",\"type\":\"%4$s\"}],"
      + "\"entities\":[{\"objectClassName\":\"entity\",\"roles\":[\"abuse\"],"
      + "\"links\":[{\"value\":\"%1$s\",\"rel\":\"%2$s\",\"href\":\"%3$s\",\"type\":\"%4$s\"}]}]}]}";

  private static final String HREF = "https://ts-wire-mock.icann.org/rdap/v1/code_10403/domain/orbot.com";
  private static final String TYPE = "application/rdap+json";
  private static final String DATE = "2025-02-18T20:34:24Z";

  private static JsonNode json(String s) throws Exception {
    return MAPPER.readTree(s);
  }

  private static JsonNode doc(String value, String rel, String href, String type, String lastUpdate) throws Exception {
    return json(String.format(BASE, value, rel, href, type, lastUpdate));
  }

  private static JsonNode original() throws Exception {
    return doc(HREF, "self", HREF, TYPE, DATE);
  }

  // ---------------------------------------------------------------- links: must NOT differ

  @Test
  public void linkValue_differentText_ignored() throws Exception {
    assertThat(comparator.compare(original(),
        doc("https://ts-wire-mock.icann.org/rdap/domain/orkut.com", "self", HREF, TYPE, DATE))).isZero();
  }

  @Test
  public void linkValue_presentVsAbsent_ignored() throws Exception {
    JsonNode a = json("{\"links\":[{\"value\":\"x\",\"rel\":\"self\",\"href\":\"h\",\"type\":\"t\"}]}");
    JsonNode b = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h\",\"type\":\"t\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void linkHref_caseOnly_ignored() throws Exception {
    assertThat(comparator.compare(original(),
        doc(HREF, "self", "https://ts-wire-mock.icann.org/rdap/v1/code_10403/domain/oRbOt.cOm", TYPE, DATE))).isZero();
  }

  @Test
  public void linkRelAndType_caseOnly_ignored() throws Exception {
    assertThat(comparator.compare(original(),
        doc(HREF, "SELF", HREF, "Application/RDAP+JSON", DATE))).isZero();
  }

  @Test
  public void linkProperties_differentOrder_ignored() throws Exception {
    JsonNode a = json("{\"links\":[{\"value\":\"v\",\"rel\":\"self\",\"href\":\"h\",\"type\":\"t\"}]}");
    JsonNode b = json("{\"links\":[{\"type\":\"t\",\"href\":\"h\",\"rel\":\"self\",\"value\":\"v\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void links_differentOrder_ignored() throws Exception {
    JsonNode a = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h1\"},{\"rel\":\"related\",\"href\":\"h2\"}]}");
    JsonNode b = json("{\"links\":[{\"rel\":\"related\",\"href\":\"h2\"},{\"rel\":\"self\",\"href\":\"h1\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void duplicateLinks_differingOnlyInValue_ignored() throws Exception {
    JsonNode a = json("{\"links\":[{\"value\":\"a\",\"rel\":\"self\",\"href\":\"h\"},{\"value\":\"b\",\"rel\":\"self\",\"href\":\"h\"}]}");
    JsonNode b = json("{\"links\":[{\"value\":\"c\",\"rel\":\"self\",\"href\":\"h\"},{\"value\":\"c\",\"rel\":\"self\",\"href\":\"h\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void duplicateNotices_differingOnlyInLinkValue_ignored() throws Exception {
    JsonNode a = json("{\"notices\":[{\"title\":\"T\",\"links\":[{\"value\":\"a\",\"href\":\"h\"}]},"
        + "{\"title\":\"T\",\"links\":[{\"value\":\"b\",\"href\":\"h\"}]}]}");
    JsonNode b = json("{\"notices\":[{\"title\":\"T\",\"links\":[{\"value\":\"c\",\"href\":\"h\"}]},"
        + "{\"title\":\"T\",\"links\":[{\"value\":\"c\",\"href\":\"h\"}]}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void linkHrefNull_inBoth_ignored() throws Exception {
    JsonNode a = json("{\"links\":[{\"rel\":\"self\",\"href\":null}]}");
    JsonNode b = json("{\"links\":[{\"rel\":\"self\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  // ---------------------------------------------------------------- links: MUST differ

  @Test
  public void linkHref_differentText_ignored() throws Exception {
    assertThat(comparator.compare(original(),
        doc(HREF, "self", "https://ts-wire-mock.icann.org/rdap/v1/code_10403/domain/otro.com", TYPE, DATE))).isZero();
  }

  @Test
  public void linkHref_percentEncodingDiffers_ignored() throws Exception {
    assertThat(comparator.compare(original(),
        doc(HREF, "self", HREF.replace("orbot", "orb%6Ft"), TYPE, DATE))).isZero();
  }

  @Test
  public void linkHref_trailingSlashDiffers_ignored() throws Exception {
    assertThat(comparator.compare(original(), doc(HREF, "self", HREF + "/", TYPE, DATE))).isZero();
  }

  @Test
  public void linkHref_presentVsAbsent_ignored() throws Exception {
    JsonNode a = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h\"}]}");
    JsonNode b = json("{\"links\":[{\"rel\":\"self\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void nestedLinkHref_inEntityOfEntity_ignored() throws Exception {
    JsonNode a = json("{\"entities\":[{\"entities\":[{\"links\":[{\"rel\":\"about\",\"href\":\"h1\"}]}]}]}");
    JsonNode b = json("{\"entities\":[{\"entities\":[{\"links\":[{\"rel\":\"about\",\"href\":\"h2\"}]}]}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void nestedLinkRel_inEntityOfEntity_detected() throws Exception {
    JsonNode a = json("{\"entities\":[{\"entities\":[{\"links\":[{\"rel\":\"about\",\"href\":\"h\"}]}]}]}");
    JsonNode b = json("{\"entities\":[{\"entities\":[{\"links\":[{\"rel\":\"self\",\"href\":\"h\"}]}]}]}");
    assertThat(comparator.compare(a, b)).isNotZero();
  }

  @Test
  public void linkRel_differentText_detected() throws Exception {
    assertThat(comparator.compare(original(), doc(HREF, "alternate", HREF, TYPE, DATE))).isNotZero();
  }

  @Test
  public void linkType_differentText_detected() throws Exception {
    assertThat(comparator.compare(original(), doc(HREF, "self", HREF, "text/html", DATE))).isNotZero();
  }

  @Test
  public void linkRel_presentVsAbsent_detected() throws Exception {
    JsonNode a = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h\"}]}");
    JsonNode b = json("{\"links\":[{\"href\":\"h\"}]}");
    assertThat(comparator.compare(a, b)).isNotZero();
  }

  @Test
  public void linkType_presentVsAbsent_detected() throws Exception {
    JsonNode a = json("{\"links\":[{\"rel\":\"self\",\"type\":\"t\"}]}");
    JsonNode b = json("{\"links\":[{\"rel\":\"self\"}]}");
    assertThat(comparator.compare(a, b)).isNotZero();
  }

  @Test
  public void links_differentCount_detected() throws Exception {
    JsonNode a = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h\"}]}");
    JsonNode b = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h\"},{\"rel\":\"related\",\"href\":\"h2\"}]}");
    assertThat(comparator.compare(a, b)).isNotZero();
  }

  @Test
  public void linkTitle_differentText_detected() throws Exception {
    JsonNode a = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h\",\"title\":\"A\"}]}");
    JsonNode b = json("{\"links\":[{\"rel\":\"self\",\"href\":\"h\",\"title\":\"B\"}]}");
    assertThat(comparator.compare(a, b)).isNotZero();
  }

  @Test
  public void linkOnlyValueAndHref_alwaysEqual() throws Exception {
    JsonNode a = json("{\"links\":[{\"value\":\"a\",\"href\":\"h1\"}]}");
    JsonNode b = json("{\"links\":[{\"value\":\"b\",\"href\":\"h2\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void linksNotAnArray_fallsBackToGenericComparison() throws Exception {
    JsonNode a = json("{\"links\":\"x\"}");
    assertThat(comparator.compare(a, json("{\"links\":\"X\"}"))).isZero();
    assertThat(comparator.compare(a, json("{\"links\":\"y\"}"))).isNotZero();
  }

  // ---------------------------------------------------------------- generic rules

  @Test
  public void lastUpdateOfRdapDatabase_differentDate_ignored() throws Exception {
    assertThat(comparator.compare(original(), doc(HREF, "self", HREF, TYPE, "2025-02-19T20:34:24Z"))).isZero();
  }

  @Test
  public void otherEvent_differentDate_detected() throws Exception {
    JsonNode b = original();
    JsonNode modified = json(b.toString().replace("1997-09-15T04:00:00Z", "1998-09-15T04:00:00Z"));
    assertThat(comparator.compare(original(), modified)).isNotZero();
  }

  @Test
  public void arrayOrder_ignored() throws Exception {
    JsonNode b = json("{\"status\":[\"b\",\"a\"],\"nameservers\":[{\"ldhName\":\"NS2\"},{\"ldhName\":\"NS1\"}]}");
    JsonNode a = json("{\"status\":[\"a\",\"b\"],\"nameservers\":[{\"ldhName\":\"NS1\"},{\"ldhName\":\"NS2\"}]}");
    assertThat(comparator.compare(a, b)).isZero();
  }

  @Test
  public void propertyOrder_ignored() throws Exception {
    assertThat(comparator.compare(json("{\"a\":1,\"b\":{\"c\":2,\"d\":3}}"),
        json("{\"b\":{\"d\":3,\"c\":2},\"a\":1}"))).isZero();
  }

  @Test
  public void identicalDocuments_equal() throws Exception {
    assertThat(comparator.compare(original(), original())).isZero();
  }

  @DataProvider
  public Object[][] realDifferences() {
    return new Object[][]{
        {"\"handle\":\"H-1\"", "\"handle\":\"H-2\""},
        {"\"ldhName\":\"orbot.com\"", "\"ldhName\":\"different.com\""},
        {"\"client transfer prohibited\"", "\"server hold\""},
        {"\"ldhName\":\"NS2.orbot.com\"", "\"ldhName\":\"NS9.orbot.com\""},
        {"\"handle\":\"292\"", "\"handle\":\"293\""},
    };
  }

  @Test(dataProvider = "realDifferences")
  public void realDifference_detected(String from, String to) throws Exception {
    JsonNode modified = json(original().toString().replace(from, to));
    assertThat(comparator.compare(original(), modified)).isNotZero();
  }

  @Test
  public void caseOnlyDifference_outsideLinks_ignored() throws Exception {
    JsonNode modified = json(original().toString().replace("\"handle\":\"H-1\"", "\"handle\":\"h-1\""));
    assertThat(comparator.compare(original(), modified)).isZero();
  }

  @Test
  public void missingProperty_detected() throws Exception {
    assertThat(comparator.compare(json("{\"a\":1,\"b\":2}"), json("{\"a\":1}"))).isNotZero();
  }

  @Test
  public void nullVsString_detected() throws Exception {
    assertThat(comparator.compare(json("{\"a\":null}"), json("{\"a\":\"null\"}"))).isNotZero();
    assertThat(comparator.compare(json("{\"a\":null}"), json("{\"a\":\"x\"}"))).isNotZero();
    assertThat(comparator.compare(json("{\"a\":null}"), json("{\"a\":null}"))).isZero();
  }

  @Test
  public void duplicateElements_differentMultiplicity_detected() throws Exception {
    assertThat(comparator.compare(json("{\"status\":[\"a\",\"a\"]}"), json("{\"status\":[\"a\"]}"))).isNotZero();
  }

  @Test
  public void duplicateElements_sameMultiplicity_ignored() throws Exception {
    assertThat(comparator.compare(json("{\"status\":[\"a\",\"a\",\"b\"]}"), json("{\"status\":[\"b\",\"A\",\"a\"]}"))).isZero();
  }
}

