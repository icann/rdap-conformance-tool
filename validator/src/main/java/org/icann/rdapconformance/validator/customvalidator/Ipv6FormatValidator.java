package org.icann.rdapconformance.validator.customvalidator;

import org.everit.json.schema.internal.IPV6Validator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DatasetValidator;

public class Ipv6FormatValidator extends IpFormatValidator {

  public static final String NOT_ALLOCATED_NOR_LEGACY = "The IPv6 address is not included in a "
      + "prefix categorized as Global Unicast in the Internet Protocol Version 6 Address Space. Dataset: ipv6AddressSpace";
  public static final String PART_OF_SPECIAL_ADDRESSES = "The IPv6 address is included in the IANA IPv6 Special-Purpose Address Registry. Dataset: specialIPv6Addresses";

  public Ipv6FormatValidator(
      DatasetValidator datasetValidator,
      DatasetValidator specialIpAddresses) {
    super(datasetValidator, specialIpAddresses, new IPV6Validator());
  }


  @Override
  public String formatName() {
    return "ipv6-validation";
  }

  @Override
  protected String getPartOfSpecialAddressesSpaceError() {
    return PART_OF_SPECIAL_ADDRESSES;
  }

  @Override
  protected String getNotAllocatedNorLegacyError() {
    return NOT_ALLOCATED_NOR_LEGACY;
  }
}
