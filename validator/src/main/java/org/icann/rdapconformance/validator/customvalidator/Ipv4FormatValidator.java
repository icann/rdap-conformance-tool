package org.icann.rdapconformance.validator.customvalidator;

import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.internal.IPV4Validator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ipv4FormatValidator implements FormatValidator {

  public static String NOT_ALLOCATED_NOR_LEGACY = "The IPv4 address is not included in a prefix categorized as "
      + "ALLOCATED or LEGACY in the IANA IPv4 Address Space Registry. Dataset: "
      + "ipv4AddressSpace";
  public static String PART_OF_SPECIAL_ADDRESSES = "The IPv4 address is included in the IANA IPv4 Special-Purpose  Address Registry. Dataset: specialIPv4Addresses";
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
      return Optional.of(NOT_ALLOCATED_NOR_LEGACY);
    }

    if (specialIPv4Addresses.isInvalid(subject)) {
      logger.error("IP address " + subject + " is part of the specialIPv4Addresses");
      return Optional.of(PART_OF_SPECIAL_ADDRESSES);
    }

    return Optional.empty();
  }

  @Override
  public String formatName() {
    return "ipv4-validation";
  }
}
