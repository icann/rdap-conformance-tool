package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import inet.ipaddr.IPAddressString;

public class SpecialIPv4Addresses extends IpAddressSpecialRegistry {

  public boolean isInvalid(String ip) {
    return getValues().stream().anyMatch(specialIp -> {
      IPAddressString net = new IPAddressString(specialIp);
      return net.contains(new IPAddressString(ip));
    });
  }
}
