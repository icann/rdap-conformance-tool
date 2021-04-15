package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;

public class EPPRoidDataset extends RDAPDataset<EPPRoid> {

  public EPPRoidDataset(FileSystem fileSystem) {
    super("EPPROID",
        URI.create("https://www.iana.org/assignments/epp-repository-ids/epp-repository-ids.xml"),
        fileSystem, EPPRoid.class);
  }
}
