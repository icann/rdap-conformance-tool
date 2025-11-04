package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;

public class IPv6AddressSpaceDataset extends RDAPDataset<Ipv6AddressSpace> {

  public IPv6AddressSpaceDataset(FileSystem fileSystem) {
    super("ipv6AddressSpace",
        URI.create("https://www.iana.org/assignments/ipv6-address-space/ipv6-address-space.xml"),
        fileSystem, Ipv6AddressSpace.class);
  }

  public IPv6AddressSpaceDataset(FileSystem fileSystem, String datasetDirectory) {
    super("ipv6AddressSpace",
        URI.create("https://www.iana.org/assignments/ipv6-address-space/ipv6-address-space.xml"),
        fileSystem, datasetDirectory, Ipv6AddressSpace.class);
  }
}
