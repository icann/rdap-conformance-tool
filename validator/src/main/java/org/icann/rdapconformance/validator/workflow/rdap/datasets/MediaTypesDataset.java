package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class MediaTypesDataset extends RDAPDataset {

  public MediaTypesDataset(FileSystem fileSystem) {
    super("mediaTypes",
        URI.create("https://www.iana.org/assignments/media-types/media-types.xml"),
        fileSystem);
  }
}
