package org.icann.rdapconformance.validator.workflow.rdap;

import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.*;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPDatasetServiceImpl implements RDAPDatasetService {

  private static final Logger logger = LoggerFactory.getLogger(RDAPDatasetService.class);
  public static final int HANG_TIMEOUT = 5;
  public static final int AWAIT_TIMEOUT = 30;
  public static final int THREADS_PER_CORE = 2;
  public static final int MAX_THREAD_POOL_SIZE = 8;
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

    // Create a thread pool for parallel dataset loading
    // Use a thread pool size based on available processors but cap it to avoid overwhelming the system
    int threadPoolSize = Math.min(Runtime.getRuntime().availableProcessors() * THREADS_PER_CORE, MAX_THREAD_POOL_SIZE);
    ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
    
    try {
      // Create CompletableFutures for each dataset download and parse operation
      List<CompletableFuture<Boolean>> datasetFutures = this.datasets.values().stream()
          .map(dataset -> CompletableFuture
              .supplyAsync(() -> {
                logger.info("Starting download for dataset: {}", dataset.getName());
                if (!dataset.download(useLocalDatasets)) {
                  logger.error("Failed to download dataset {}", dataset.getName());
                  return false;
                }
                logger.info("Download completed for dataset: {}", dataset.getName());
                return true;
              }, executor)
              .thenApplyAsync(downloadSuccess -> {
                if (!downloadSuccess) {
                  return false;
                }
                logger.info("Starting parse for dataset: {}", dataset.getName());
                if (!dataset.parse()) {
                  logger.error("Failed to parse dataset {}", dataset.getName());
                  return false;
                }
                logger.info("Parse completed for dataset: {}", dataset.getName());
                return true;
              }, executor)
              .exceptionally(throwable -> {
                logger.error("Exception occurred while processing dataset {}: {}", 
                    dataset.getName(), throwable.getMessage(), throwable);
                return false;
              })
          )
          .collect(Collectors.toList());

      // Wait for all datasets to complete and check if all succeeded
      CompletableFuture<Void> allFutures = CompletableFuture.allOf(
          datasetFutures.toArray(new CompletableFuture[ZERO]));
      
      // Wait for all to complete (with timeout to prevent hanging)
      allFutures.get(HANG_TIMEOUT, TimeUnit.MINUTES);
      
      // Check if all datasets were successful
      boolean allSuccessful = datasetFutures.stream()
          .map(CompletableFuture::join)
          .allMatch(success -> success);
      
      if (!allSuccessful) {
        logger.error("One or more datasets failed to download or parse");
        return false;
      }
      
      logger.info("All datasets downloaded and parsed successfully");
      
    } catch (Exception e) {
      logger.error("Failed to download datasets in parallel", e);
      return false;
    } finally {
      // Shutdown the executor
      executor.shutdown();
      try {
        if (!executor.awaitTermination(AWAIT_TIMEOUT, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
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