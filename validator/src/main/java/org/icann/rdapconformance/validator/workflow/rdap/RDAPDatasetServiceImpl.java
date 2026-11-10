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
import org.icann.rdapconformance.validator.ProgressCallback;
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
  private final String datasetDirectory;
  private final List<RDAPDataset<? extends RDAPDatasetModel>> datasetList;
  protected Map<Class<? extends RDAPDataset>, RDAPDataset> datasets;
  protected Map<Class<?>, Object> datasetValidatorModels;

  // Public constructor for instance-based usage with default dataset directory
  public RDAPDatasetServiceImpl(FileSystem fileSystem) {
    this(fileSystem, null);
  }

  // Public constructor for instance-based usage with custom dataset directory
  public RDAPDatasetServiceImpl(FileSystem fileSystem, String datasetDirectory) {
    this.fileSystem = fileSystem;
    this.datasetDirectory = datasetDirectory != null ? datasetDirectory : DATASET_PATH;
    datasetList = List.of(new IPv4AddressSpaceDataset(fileSystem, this.datasetDirectory),
        new SpecialIPv4AddressesDataset(fileSystem, this.datasetDirectory),
        new IPv6AddressSpaceDataset(fileSystem, this.datasetDirectory),
        new SpecialIPv6AddressesDataset(fileSystem, this.datasetDirectory),
        new RDAPExtensionsDataset(fileSystem, this.datasetDirectory),
        new LinkRelationsDataset(fileSystem, this.datasetDirectory),
        new MediaTypesDataset(fileSystem, this.datasetDirectory),
        new RDAPJsonValuesDataset(fileSystem, this.datasetDirectory),
        new DsRrTypesDataset(fileSystem, this.datasetDirectory),
        new DNSSecAlgNumbersDataset(fileSystem, this.datasetDirectory),
        new BootstrapDomainNameSpaceDataset(fileSystem, this.datasetDirectory),
        new RegistrarIdDataset(fileSystem, this.datasetDirectory),
        new EPPRoidDataset(fileSystem, this.datasetDirectory));
    this.datasets = datasetList
        .stream()
        .collect(Collectors.toMap(RDAPDataset::getClass, Function.identity()));
  }


  /**
   * Download all RDAP datasets.
   *
   * @param useLocalDatasets Whether local versions of datasets are used instead of downloading them
   *                         again
   */
  public boolean download(boolean useLocalDatasets) {
    return download(useLocalDatasets, null);
  }

  /**
   * Download all RDAP datasets with progress callback.
   *
   * @param useLocalDatasets Whether local versions of datasets are used instead of downloading them
   *                         again
   * @param progressCallback Callback to receive progress updates, or null for no progress tracking
   */
  public boolean download(boolean useLocalDatasets, ProgressCallback progressCallback) {
    try {
      fileSystem.mkdir(datasetDirectory);
    } catch (IOException e) {
      logger.error("Failed to create datasets directory: {}", datasetDirectory, e);
      return false;
    }

    // Create a thread pool for parallel dataset loading
    // Use a thread pool size based on available processors but cap it to avoid overwhelming the system
    int threadPoolSize = Math.min(Runtime.getRuntime().availableProcessors() * THREADS_PER_CORE, MAX_THREAD_POOL_SIZE);

    logger.info("Starting dataset download and parse operations...");

    if (!downloadAndParseAll(threadPoolSize, useLocalDatasets, progressCallback)) {
      return false;
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

  private boolean downloadAndParseAll(int threadPoolSize, boolean useLocalDatasets,
      ProgressCallback progressCallback) {
    // try-with-resources guarantees the executor is closed on every exit path
    try (ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize)) {
      try {
        List<CompletableFuture<Boolean>> datasetFutures = this.datasets.values().stream()
            .map(dataset -> processDataset(dataset, useLocalDatasets, progressCallback, executor))
            .collect(Collectors.toList());

        // Wait for all to complete (with timeout to prevent hanging)
        CompletableFuture.allOf(datasetFutures.toArray(new CompletableFuture[ZERO]))
            .get(HANG_TIMEOUT, TimeUnit.MINUTES);

        boolean allSuccessful = datasetFutures.stream()
            .map(CompletableFuture::join)
            .allMatch(Boolean::booleanValue);
        if (!allSuccessful) {
          logger.error("One or more datasets failed to download or parse");
          return false;
        }
        logger.info("All datasets downloaded and parsed successfully");
        return true;
      } catch (InterruptedException e) {
        logger.error("Interrupted while downloading datasets", e);
        executor.shutdownNow();
        Thread.currentThread().interrupt();
        return false;
      } catch (Exception e) {
        logger.error("Failed to download datasets in parallel", e);
        // Interrupt any still-running tasks so close() doesn't block
        executor.shutdownNow();
        return false;
      }
    }
  }

  private CompletableFuture<Boolean> processDataset(RDAPDataset<?> dataset,
      boolean useLocalDatasets, ProgressCallback progressCallback, ExecutorService executor) {
    return CompletableFuture
        .supplyAsync(() -> downloadDataset(dataset, useLocalDatasets, progressCallback), executor)
        .thenApplyAsync(ok -> ok && parseDataset(dataset, progressCallback), executor)
        .exceptionally(throwable -> {
          logger.error("Exception occurred while processing dataset {}: {}",
              dataset.getName(), throwable.getMessage(), throwable);
          if (progressCallback != null) {
            progressCallback.onDatasetError(dataset.getName(), "processing", throwable);
          }
          return false;
        });
  }

  private boolean downloadDataset(RDAPDataset<?> dataset, boolean useLocalDatasets,
      ProgressCallback progressCallback) {
    logger.debug("Starting download for dataset: {}", dataset.getName());
    if (progressCallback != null) {
      progressCallback.onDatasetDownloadStarted(dataset.getName());
    }
    if (!dataset.download(useLocalDatasets)) {
      logger.error("Failed to download dataset {}", dataset.getName());
      if (progressCallback != null) {
        progressCallback.onDatasetError(dataset.getName(), "download",
            new RuntimeException("Download failed"));
      }
      return false;
    }
    logger.debug("Download completed for dataset: {}", dataset.getName());
    if (progressCallback != null) {
      progressCallback.onDatasetDownloadCompleted(dataset.getName());
    }
    return true;
  }

  private boolean parseDataset(RDAPDataset<?> dataset, ProgressCallback progressCallback) {
    logger.debug("Starting parse for dataset: {}", dataset.getName());
    if (progressCallback != null) {
      progressCallback.onDatasetParseStarted(dataset.getName());
    }
    if (!dataset.parse()) {
      logger.error("Failed to parse dataset {}", dataset.getName());
      if (progressCallback != null) {
        progressCallback.onDatasetError(dataset.getName(), "parse",
            new RuntimeException("Parse failed"));
      }
      return false;
    }
    logger.debug("Parse completed for dataset: {}", dataset.getName());
    if (progressCallback != null) {
      progressCallback.onDatasetParseCompleted(dataset.getName());
    }
    return true;
  }
}