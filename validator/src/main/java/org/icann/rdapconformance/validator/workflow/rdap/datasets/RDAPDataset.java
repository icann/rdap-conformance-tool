package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import static org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService.DATASET_PATH;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPDataset {

  private static final Logger logger = LoggerFactory.getLogger(RDAPDataset.class);

  private final String name;
  private final URI uri;
  private final FileSystem fileSystem;

  public RDAPDataset(String name, URI uri, FileSystem fileSystem) {
    this.name = name;
    this.fileSystem = fileSystem;
    this.uri = uri;
  }

  private String filename() {
    return uri.toString().substring(uri.toString().lastIndexOf('/') + 1);
  }

  private String filePath() {
    return Paths.get(DATASET_PATH, filename()).toAbsolutePath().toString();
  }

  public boolean download(boolean useLocalDatasets) {
    String path = filePath();
    if (useLocalDatasets && this.fileSystem.exists(path)) {
      logger.debug("Dataset {} is already downloaded", getName());
      return true;
    }
    logger.debug("Download dataset {}", getName());
    try {
      fileSystem.download(uri, path);
    } catch (IOException e) {
      logger.error("Failed to download dataset {}", name, e);
      return false;
    }
    return true;
  }

  public String getName() {
    return this.name;
  }
}
