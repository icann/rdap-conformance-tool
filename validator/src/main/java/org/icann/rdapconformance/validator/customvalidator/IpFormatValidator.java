package org.icann.rdapconformance.validator.customvalidator;

import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.IpAddressSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IpFormatValidator implements FormatValidator {

  private static final Logger logger = LoggerFactory.getLogger(IpFormatValidator.class);

  private final IpAddressSpace ipAddressSpace;
  private final IpAddressSpace specialIpAddresses;
  private final FormatValidator ipValidator;

  public IpFormatValidator(IpAddressSpace ipAddressSpace,
      IpAddressSpace specialIpAddresses,
      FormatValidator ipValidator) {
    this.ipAddressSpace = ipAddressSpace;
    this.specialIpAddresses = specialIpAddresses;
    this.ipValidator = ipValidator;
  }

  @Override
  public Optional<String> validate(String subject) {
    Optional<String> invalidIpv4 = ipValidator.validate(subject);
    if (invalidIpv4.isPresent()) {
      return invalidIpv4;
    }

    if (ipAddressSpace.isInvalid(subject)) {
      logger.error("IP address " + subject + " is not part of a prefix categorized as ALLOCATED or "
          + "LEGACY");
      return Optional.of(getNotAllocatedNorLegacyError());
    }

    if (specialIpAddresses.isInvalid(subject)) {
      logger.error("IP address " + subject + " is part of the " + specialIpAddresses.getClass().getSimpleName());
      return Optional.of(getPartOfSpecialAddressesSpaceError());
    }

    return Optional.empty();
  }

  protected abstract String getPartOfSpecialAddressesSpaceError();

  protected abstract String getNotAllocatedNorLegacyError();
}
