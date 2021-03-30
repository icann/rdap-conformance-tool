package org.icann.rdapconformance.validator;

import java.util.List;

public class SchemaValidatorDomainTest extends SchemaValidatorTest {

  public SchemaValidatorDomainTest() {
    super(
        "domain",
        "rdap_domain.json",
        "/validators/domain/valid.json",
        -12200,
        -12201,
        -12202,
        -12213,
        -12219,
        List.of("objectClassName", "handle",
            "ldhName", "unicodeName", "variants", "nameservers", "secureDNS", "entities", "status",
            "publicIds", "remarks", "links", "port43", "events", "notices", "rdapConformance",
            "lang"));
  }
}