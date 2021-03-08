package org.icann.rdap.conformance.validator.validators;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.icann.rdap.conformance.validator.RDAPDeserializer;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.configuration.ConfigurationFile;
import org.icann.rdap.conformance.validator.models.domain.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdRdapDomainLookupValidation implements Validator {

  private final static Set<String> AUTHORIZED_KEYS = Set.of("objectClassName", "handle", "ldhName",
      "unicodeName", "variants", "nameservers", "secureDNS", "entities", "status", "publicIds",
      "remarks", "links", "port43", "events", "notices", "rdapConformance");
  private final Logger logger = LoggerFactory.getLogger(StdRdapDomainLookupValidation.class);
  private final RDAPDeserializer deserializer;
  private final String rdapResponse;
  private final ConfigurationFile configurationFile;

  public StdRdapDomainLookupValidation(String rdapResponse, ConfigurationFile configurationFile) {
    // TODO specialize RDAPDeserializer if needed
    this.deserializer = new RDAPDeserializer();
    this.rdapResponse = rdapResponse;
    this.configurationFile = configurationFile;
  }

  public List<RDAPValidationResult> validate() {
    List<RDAPValidationResult> results = new ArrayList<>();
    Map<String, Object> rawRdap;
    Domain rdapDomain;
    try {
      rawRdap = this.deserializer.deserialize(rdapResponse, Map.class);
      rdapDomain = this.deserializer.deserialize(rdapResponse, Domain.class);
    } catch (JsonProcessingException e) {
      // 1. The domain data structure must be a syntactically valid JSON object.
      logger.error("Failed to deserialize RDAP response", e);
      results.add(RDAPValidationResult.builder()
          .code("-12199-12199 - 1 -12200")
          .value(this.rdapResponse)
          .message("The domain structure is not syntactically valid.")
          .build());
      return results;
    }

    for (String key : rawRdap.keySet()) {
      if (!AUTHORIZED_KEYS.contains(key)) {
        // 2. The name of every name/value pairs shall be any of:
        // objectClassName, handle, ldhName, unicodeName, variants, nameservers, secureDNS, entities,
        // status, publicIds, remarks, links, port43, events, notices or rdapConformance.
        logger.error("Unrecognized key {} in RDAP response", key);
        results.add(RDAPValidationResult.builder()
            .code("-12199 - 2 -12201")
            .value(key + "/" + rawRdap.get(key).toString())
            .message("The name in the name/value pair is not of: objectClassName, handle, ldhName, "
                + "unicodeName, variants, nameservers, secureDNS, entities, status, publicIds, "
                + "remarks, links, port43, events, notices or rdapConformance.")
            .build());
      }
    }

    results.addAll(rdapDomain.validate(this.configurationFile));
    return results;
  }
}
