package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;

public class MediaTypesDataset extends RDAPDataset<MediaTypes> {

  public MediaTypesDataset(FileSystem fileSystem) {
    super("mediaTypes",
        URI.create("https://www.iana.org/assignments/media-types/media-types.xml"),
        fileSystem, MediaTypes.class);
  }

  public MediaTypesDataset(FileSystem fileSystem, String datasetDirectory) {
    super("mediaTypes",
        URI.create("https://www.iana.org/assignments/media-types/media-types.xml"),
        fileSystem, datasetDirectory, MediaTypes.class);
  }
}
