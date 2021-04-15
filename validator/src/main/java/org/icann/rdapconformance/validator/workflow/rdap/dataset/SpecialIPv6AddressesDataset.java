package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;

public class SpecialIPv6AddressesDataset extends RDAPDataset<SpecialIPv6Addresses> {

  public SpecialIPv6AddressesDataset(FileSystem fileSystem) {
    super("specialIPv6Addresses",
        URI.create(
            "https://www.iana.org/assignments/iana-ipv6-special-registry/iana-ipv6-special-registry.xml"),
        fileSystem, SpecialIPv6Addresses.class);
  }
}
