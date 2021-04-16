package org.icann.rdapconformance.validator.customvalidator;

import org.everit.json.schema.internal.IPV4Validator;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DatasetValidator;

public class Ipv4FormatValidator extends IpFormatValidator {

  public static final String NOT_ALLOCATED_NOR_LEGACY = "The IPv4 address is not included in a prefix "
      + "categorized as "
      + "ALLOCATED or LEGACY in the IANA IPv4 Address Space Registry. Dataset: "
      + "ipv4AddressSpace";
  public static final String PART_OF_SPECIAL_ADDRESSES = "The IPv4 address is included in the IANA "
      + "IPv4 Special-Purpose  Address Registry. Dataset: specialIPv4Addresses";

  public Ipv4FormatValidator(
      DatasetValidator datasetValidator,
      DatasetValidator specialIpAddresses) {
    super(datasetValidator, specialIpAddresses, new IPV4Validator());
  }


  @Override
  public String formatName() {
    return "ipv4-validation";
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
