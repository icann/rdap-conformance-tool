package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;

public class SpecialIPv4AddressesDataset extends RDAPDataset<SpecialIPv4Addresses> {

  public SpecialIPv4AddressesDataset(FileSystem fileSystem) {
    super("specialIPv4Addresses",
        URI.create("https://www.iana.org/assignments/iana-ipv4-special-registry/iana-ipv4-special-registry.xml"),
        fileSystem, SpecialIPv4Addresses.class);
  }

  public SpecialIPv4AddressesDataset(FileSystem fileSystem, String datasetDirectory) {
    super("specialIPv4Addresses",
        URI.create("https://www.iana.org/assignments/iana-ipv4-special-registry/iana-ipv4-special-registry.xml"),
        fileSystem, datasetDirectory, SpecialIPv4Addresses.class);
  }
}
