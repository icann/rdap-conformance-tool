package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class EPPRoidDataset extends RDAPDataset {

  public EPPRoidDataset(FileSystem fileSystem) {
    super("EPPROID",
        URI.create("https://www.iana.org/assignments/epp-repository-ids/epp-repository-ids.xml"),
        fileSystem);
  }
}
