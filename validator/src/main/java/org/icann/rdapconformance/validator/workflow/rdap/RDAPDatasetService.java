package org.icann.rdapconformance.validator.workflow.rdap;

public interface RDAPDatasetService {

  String DATASET_PATH = "datasets";

  /**
   * Download all RDAP datasets.
   *
   * @param useLocalDatasets Whether local versions of datasets are used instead of downloading them
   *                         again
   */
  boolean download(boolean useLocalDatasets);

  <T> T get(Class<T> clazz);
}
