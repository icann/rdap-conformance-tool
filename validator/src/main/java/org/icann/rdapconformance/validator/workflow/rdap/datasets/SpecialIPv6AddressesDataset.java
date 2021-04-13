package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class SpecialIPv6AddressesDataset extends RDAPDataset {

  public SpecialIPv6AddressesDataset(FileSystem fileSystem) {
    super("specialIPv6Addresses",
        URI.create("https://www.iana.org/assignments/iana-ipv6-special-registry/iana-ipv6-special-registry.xml"),
        fileSystem);
  }
}
