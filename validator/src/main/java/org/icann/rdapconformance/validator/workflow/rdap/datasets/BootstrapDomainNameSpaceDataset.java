package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class BootstrapDomainNameSpaceDataset extends RDAPDataset {

  public BootstrapDomainNameSpaceDataset(FileSystem fileSystem) {
    super("bootstrapDomainNameSpace",
        URI.create("https://data.iana.org/rdap/dns.json"),
        fileSystem);
  }
}
