package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import static org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService.DATASET_PATH;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Paths;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPDatasetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RDAPDataset<T extends RDAPDatasetModel> {

  private static final Logger logger = LoggerFactory.getLogger(RDAPDataset.class);

  private final String name;
  private final URI uri;
  private final FileSystem fileSystem;
  private final T modelInstance;

  public RDAPDataset(String name, URI uri, FileSystem fileSystem, Class<T> model) {
    this.name = name;
    this.fileSystem = fileSystem;
    this.uri = uri;
    try {
      this.modelInstance = model.getConstructor().newInstance();
    } catch (Exception e) {
      logger.error("Cannot create an instance of dataset model {}", model.getSimpleName(), e);
      throw new RuntimeException();
    }
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
      logger.debug("Dataset {} is already downloaded", name);
      return true;
    }
    logger.debug("Download dataset {}", name);
    try {
      fileSystem.download(uri, path);
    } catch (IOException e) {
      logger.error("Failed to download dataset {}", name, e);
      return false;
    }
    return true;
  }

  public boolean parse() {
    String path = filePath();
    try (InputStream fis = new FileInputStream(path)) {
      this.modelInstance.parse(fis);
    } catch (Throwable t) {
      logger.error("Failed to parse dataset {}", name, t);
      return false;
    }
    return true;
  }

  public String getName() {
    return this.name;
  }

  public T getData() {
    return this.modelInstance;
  }
}
