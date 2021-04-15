package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;

public class BootstrapDomainNameSpaceDataset extends RDAPDataset<BootstrapDomainNameSpace> {

  public BootstrapDomainNameSpaceDataset(FileSystem fileSystem) {
    super("bootstrapDomainNameSpace",
        URI.create("https://data.iana.org/rdap/dns.json"),
        fileSystem, BootstrapDomainNameSpace.class);
  }
}
