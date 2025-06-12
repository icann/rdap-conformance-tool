package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.*;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPDatasetServiceImpl implements RDAPDatasetService {

  private static final Logger logger = LoggerFactory.getLogger(RDAPDatasetService.class);
  private final FileSystem fileSystem;
  private final List<RDAPDataset<? extends RDAPDatasetModel>> datasetList;
  protected Map<Class<? extends RDAPDataset>, RDAPDataset> datasets;
  protected Map<Class<?>, Object> datasetValidatorModels;

  // Singleton instance
  private static RDAPDatasetServiceImpl instance;

  // Private constructor to prevent instantiation
  private RDAPDatasetServiceImpl(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    datasetList = List.of(new IPv4AddressSpaceDataset(fileSystem),
        new SpecialIPv4AddressesDataset(fileSystem),
        new IPv6AddressSpaceDataset(fileSystem),
        new SpecialIPv6AddressesDataset(fileSystem),
        new RDAPExtensionsDataset(fileSystem),
        new LinkRelationsDataset(fileSystem),
        new MediaTypesDataset(fileSystem),
        new RDAPJsonValuesDataset(fileSystem),
        new DsRrTypesDataset(fileSystem),
        new DNSSecAlgNumbersDataset(fileSystem),
        new BootstrapDomainNameSpaceDataset(fileSystem),
        new RegistrarIdDataset(fileSystem),
        new EPPRoidDataset(fileSystem));
    this.datasets = datasetList
        .stream()
        .collect(Collectors.toMap(RDAPDataset::getClass, Function.identity()));
  }

  /**
   * Get the singleton instance of RDAPDatasetServiceImpl.
   * If the instance doesn't exist, it will be created with the provided FileSystem.
   *
   * @param fileSystem The FileSystem to use
   * @return The singleton instance of RDAPDatasetServiceImpl
   */
  public static synchronized RDAPDatasetServiceImpl getInstance(FileSystem fileSystem) {
    if (instance == null) {
      instance = new RDAPDatasetServiceImpl(fileSystem);
    }
    return instance;
  }

  /**
   * Get the singleton instance of RDAPDatasetServiceImpl.
   * This method should only be called after the instance has been initialized with a FileSystem.
   *
   * @return The singleton instance of RDAPDatasetServiceImpl
   * @throws IllegalStateException if getInstance was not previously called with a FileSystem
   */
  public static RDAPDatasetServiceImpl getInstance() {
    if (instance == null) {
      throw new IllegalStateException("RDAPDatasetServiceImpl has not been initialized. Call getInstance(FileSystem) first.");
    }
    return instance;
  }

  /**
   * Download all RDAP datasets.
   *
   * @param useLocalDatasets Whether local versions of datasets are used instead of downloading them
   *                         again
   */
  public boolean download(boolean useLocalDatasets) {
    try {
      fileSystem.mkdir(DATASET_PATH);
    } catch (IOException e) {
      logger.error("Failed to create datasets directory", e);
      return false;
    }

    for (RDAPDataset dataset : this.datasets.values()) {
      if (!dataset.download(useLocalDatasets)) {
        logger.error("Failed to download dataset {}", dataset.getName());
        return false;
      }
      if (!dataset.parse()) {
        logger.error("Failed to parse dataset {}", dataset.getName());
        return false;
      }
    }

    this.datasetValidatorModels = datasetList
        .stream()
        .map(RDAPDataset::getData)
        .collect(Collectors.toMap(RDAPDatasetModel::getClass, Function.identity()));
    // special case for these compound datasets:
    this.datasetValidatorModels.put(NoticeAndRemarkJsonValues.class,
        new NoticeAndRemarkJsonValues(get(RDAPJsonValues.class)));

    this.datasetValidatorModels.put(EventActionJsonValues.class,
        new EventActionJsonValues(get(RDAPJsonValues.class)));

    this.datasetValidatorModels.put(StatusJsonValues.class,
        new StatusJsonValues(get(RDAPJsonValues.class)));

    this.datasetValidatorModels.put(RedactedExpressionLanguageJsonValues.class,
        new RedactedExpressionLanguageJsonValues(get(RDAPJsonValues.class)));

    this.datasetValidatorModels.put(RedactedNameJsonValues.class,
        new RedactedNameJsonValues(get(RDAPJsonValues.class)));

    this.datasetValidatorModels.put(VariantRelationJsonValues.class,
        new VariantRelationJsonValues(get(RDAPJsonValues.class)));

    this.datasetValidatorModels.put(RoleJsonValues.class,
        new RoleJsonValues(get(RDAPJsonValues.class)));

    return true;
  }

  public <T> T get(Class<T> clazz) {
    return (T) this.datasetValidatorModels.get(clazz);
  }
}