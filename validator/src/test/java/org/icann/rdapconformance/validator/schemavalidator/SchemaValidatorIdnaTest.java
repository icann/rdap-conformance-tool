package org.icann.rdapconformance.validator.schemavalidator;

public abstract class SchemaValidatorIdnaTest extends SchemaValidatorTest {

  private final String label;
  private final String tld;
  private final String domain;

  protected SchemaValidatorIdnaTest(String domain) {
    super(
        "rdap_variantName.json",
        "/validators/variantName/valid.json");
    this.label = domain.split("\\.")[0];
    this.tld = domain.split("\\.")[1];
    this.domain = domain;
  }

  protected void lengthExceeding63Characters(int errorCode) {
    String sixtyThreeCharactersPlus = domain;
    for (int i = 0; i < 63; i++) {
      sixtyThreeCharactersPlus = "a" + sixtyThreeCharactersPlus;
    }
    jsonObject.put(name, sixtyThreeCharactersPlus);
    validate(errorCode, "#/" + name + ":" + sixtyThreeCharactersPlus,
        "A DNS label with length not between 1 and 63 "
            + "was found.");
  }

  protected void totalLengthExceeding253Characters(int errorCode) {
    String sixtyThreeCharacters = "";
    for (int i = 0; i < 63; i++) {
      sixtyThreeCharacters = "e" + sixtyThreeCharacters;
    }

    String tldTooLong = "a";
    for (int i = 0; i < 253 - sixtyThreeCharacters.length(); i++) {
      tldTooLong += "a";
    }

    String domain = sixtyThreeCharacters + "." + tldTooLong;
    jsonObject.put(name, domain);
    validate(errorCode, "#/" + name + ":" + domain, "A domain name of more than 253 characters was "
        + "found.");
  }

  protected void domainWithLessThan2Labels(int errorCode) {
    jsonObject.put(name, label);
    validate(errorCode, "#/" + name + ":" + label,
        "A domain name with less than two labels was found. See "
            + "RDAP_Technical_Implementation_Guide_2_1 section 1.10.");
  }

  protected void labelInvalid(int errorCode) {
    jsonObject.put(name, "xn---viagénie.ca");
    validate(errorCode, "#/" + name + ":xn---viagénie.ca",
        "A label not being a valid \"U-label\"/\"A-label\" or \"NR-LDH label\" was found. "
            + "Reasons: [PUNYCODE]");
  }
}
