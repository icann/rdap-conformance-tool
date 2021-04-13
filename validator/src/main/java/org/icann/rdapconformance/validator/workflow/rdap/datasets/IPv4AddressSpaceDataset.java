package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class IPv4AddressSpaceDataset extends RDAPDataset {

  public IPv4AddressSpaceDataset(FileSystem fileSystem) {
    super("ipv4AddressSpace",
        URI.create("https://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml"),
        fileSystem);
  }
}
