package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class SpecialIPv4AddressesDataset extends RDAPDataset {

  public SpecialIPv4AddressesDataset(FileSystem fileSystem) {
    super("specialIPv4Addresses",
        URI.create("https://www.iana.org/assignments/iana-ipv4-special-registry/iana-ipv4-special-registry.xml"),
        fileSystem);
  }
}
