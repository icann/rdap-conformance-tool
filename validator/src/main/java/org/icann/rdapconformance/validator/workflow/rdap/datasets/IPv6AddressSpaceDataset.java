package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class IPv6AddressSpaceDataset extends RDAPDataset {

  public IPv6AddressSpaceDataset(FileSystem fileSystem) {
    super("ipv6AddressSpace",
        URI.create("https://www.iana.org/assignments/ipv6-address-space/ipv6-address-space.xml"),
        fileSystem);
  }
}
