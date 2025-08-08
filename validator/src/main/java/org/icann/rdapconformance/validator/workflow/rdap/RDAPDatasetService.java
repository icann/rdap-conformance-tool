package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.ProgressCallback;

public interface RDAPDatasetService {

  String DATASET_PATH = "datasets";

  /**
   * Download all RDAP datasets.
   *
   * @param useLocalDatasets Whether local versions of datasets are used instead of downloading them
   *                         again
   */
  boolean download(boolean useLocalDatasets);

  /**
   * Download all RDAP datasets with progress callback.
   *
   * @param useLocalDatasets Whether local versions of datasets are used instead of downloading them
   *                         again
   * @param progressCallback Callback to receive progress updates, or null for no progress tracking
   */
  boolean download(boolean useLocalDatasets, ProgressCallback progressCallback);

  <T> T get(Class<T> clazz);
}
