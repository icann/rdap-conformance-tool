package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;

public class RDAPExtensionsDataset extends RDAPDataset<RDAPExtensions> {

  public RDAPExtensionsDataset(FileSystem fileSystem) {
    super("RDAPExtensions",
        URI.create("https://www.iana.org/assignments/rdap-extensions/rdap-extensions.xml"),
        fileSystem, RDAPExtensions.class);
  }
}
