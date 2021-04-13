package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class LinkRelationsDataset extends RDAPDataset {

  public LinkRelationsDataset(FileSystem fileSystem) {
    super("linkRelations",
        URI.create("https://www.iana.org/assignments/link-relations/link-relations.xml"),
        fileSystem);
  }
}
