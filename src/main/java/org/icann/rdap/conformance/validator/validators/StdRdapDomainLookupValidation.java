package org.icann.rdap.conformance.validator.validators;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.icann.rdap.conformance.validator.RDAPDeserializer;
import org.icann.rdap.conformance.validator.RDAPValidationResult;
import org.icann.rdap.conformance.validator.RDAPValidatorContext;
import org.icann.rdap.conformance.validator.models.domain.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StdRdapDomainLookupValidation extends Validator {

  private static final Logger logger = LoggerFactory.getLogger(StdRdapDomainLookupValidation.class);
  private static final Set<String> AUTHORIZED_KEYS = Set.of("objectClassName", "handle", "ldhName",
      "unicodeName", "variants", "nameservers", "secureDNS", "entities", "status", "publicIds",
      "remarks", "links", "port43", "events", "notices", "rdapConformance");

  public StdRdapDomainLookupValidation(RDAPValidatorContext context) {
    super(context);
  }


  public List<RDAPValidationResult> validate(String rdapContent) {
    logger.info("Starting stdRdapDomainLookupValidation");
    List<RDAPValidationResult> results = new ArrayList<>();
    Map<String, Object> rawRdap;
    Domain rdapDomain;
    RDAPDeserializer deserializer = this.context.getDeserializer();
    try {
      logger.debug("Deserializing domain object");
      rawRdap = deserializer.deserialize(rdapContent, Map.class);
      rdapDomain = deserializer.deserialize(rdapContent, Domain.class);
    } catch (JsonProcessingException e) {
      // 3. The JSON name/values of objectClassName, handle, ldhName, unicodeName, variants,
      // nameservers, secureDNS, entities, status, publicIds, remarks, links, port43, events,
      // notices or rdapConformance shall appear only once.
      if (e.getMessage().startsWith("Duplicate field")) {
        String keyVal = "";
        String key = "ERROR: Failed to retrieve key";
        try {
          JsonParseException exc = ((JsonParseException) e);
          key = exc.getProcessor().getCurrentName();
          String value = exc.getProcessor().getText();
          keyVal = key + "/" + value;
        } catch (IOException ex) {
          logger.error("Cannot retrieve key, value from duplicate field error", ex);
        }
        logger.error("Duplicated key '{}' in RDAP response", key);

        results.add(RDAPValidationResult.builder()
            .code(-12202)
            .value(keyVal)
            .message(
                "The name in the name/value pair of a domain structure was found more than once.")
            .build());
        return results;
      }
      // 1. The domain data structure must be a syntactically valid JSON object.
      logger.error("Failed to deserialize RDAP response", e);
      results.add(RDAPValidationResult.builder()
          .code(-12200)
          .value(rdapContent)
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
            .code(-12201)
            .value(key + "/" + rawRdap.get(key).toString())
            .message("The name in the name/value pair is not of: objectClassName, handle, ldhName, "
                + "unicodeName, variants, nameservers, secureDNS, entities, status, publicIds, "
                + "remarks, links, port43, events, notices or rdapConformance.")
            .build());
      }
    }

    logger.debug("Domain JSON is valid");

    results.addAll(rdapDomain.validate());
    return results;
  }
}
