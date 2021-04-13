package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class RDAPExtensionsDataset extends RDAPDataset {

  public RDAPExtensionsDataset(FileSystem fileSystem) {
    super("RDAPExtensions",
        URI.create("https://www.iana.org/assignments/rdap-extensions/rdap-extensions.xml"),
        fileSystem);
  }
}
