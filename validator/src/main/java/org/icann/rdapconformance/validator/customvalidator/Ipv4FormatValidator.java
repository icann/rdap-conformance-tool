package org.icann.rdapconformance.validator.customvalidator;

import inet.ipaddr.IPAddressString;
import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ipv4FormatValidator implements FormatValidator {

  private static final Logger logger = LoggerFactory.getLogger(Ipv4FormatValidator.class);

  private final Ipv4AddressSpace ipv4AddressSpace;
  private final SpecialIPv4Addresses specialIPv4Addresses;

  public Ipv4FormatValidator(Ipv4AddressSpace ipv4AddressSpace,
      SpecialIPv4Addresses specialIPv4Addresses) {
    this.ipv4AddressSpace = ipv4AddressSpace;
    this.specialIPv4Addresses = specialIPv4Addresses;
  }

  @Override
  public Optional<String> validate(String subject) {
    Optional<String> invalidIpv4 = new IPV4Validator().validate(subject);
    if (invalidIpv4.isPresent()) {
      return invalidIpv4;
    }

    if (ipv4AddressSpace.isInvalid(subject)) {
      logger.error("IP address " + subject + " is not part of a prefix categorized as ALLOCATED or "
          + "LEGACY");
      return Optional
          .of("IP address " + subject + " is not part of a prefix categorized as ALLOCATED or "
              + "LEGACY");
    }

    if (specialIPv4Addresses.getValues().stream().anyMatch(specialIp -> {
      IPAddressString net = new IPAddressString(specialIp);
      return net.contains(new IPAddressString(subject));
    })) {
      logger.error("IP address " + subject + " is part of the specialIPv4Addresses");
      return Optional.of("IP address " + subject + " is part of the specialIPv4Addresses");
    }

    return Optional.empty();
  }

  @Override
  public String formatName() {
    return "ipv4-validation";
  }
}
