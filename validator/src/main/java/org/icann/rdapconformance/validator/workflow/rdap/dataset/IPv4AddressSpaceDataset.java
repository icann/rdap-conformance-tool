package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;

public class IPv4AddressSpaceDataset extends RDAPDataset<Ipv4AddressSpace> {

  public IPv4AddressSpaceDataset(FileSystem fileSystem) {
    super("ipv4AddressSpace",
        URI.create("https://www.iana.org/assignments/ipv4-address-space/ipv4-address-space.xml"),
        fileSystem, Ipv4AddressSpace.class);
  }
}
