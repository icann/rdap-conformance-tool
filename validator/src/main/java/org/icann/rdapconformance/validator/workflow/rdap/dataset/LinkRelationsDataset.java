package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;

public class LinkRelationsDataset extends RDAPDataset<LinkRelations> {

  public LinkRelationsDataset(FileSystem fileSystem) {
    super("linkRelations",
        URI.create("https://www.iana.org/assignments/link-relations/link-relations.xml"),
        fileSystem, LinkRelations.class);
  }
}
