package org.icann.rdapconformance.validator.workflow.rdap;

import org.icann.rdapconformance.validator.ProgressCallback;

/**
 * Service interface for managing RDAP validation datasets and their lifecycle.
 *
 * <p>This interface defines the contract for services that handle RDAP dataset
 * operations including downloading, caching, and providing access to validation
 * data required for RDAP conformance testing. Implementations manage the complete
 * lifecycle of RDAP datasets from acquisition to access.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Downloading RDAP datasets from authoritative sources</li>
 *   <li>Managing local dataset caching and storage</li>
 *   <li>Providing type-safe access to specific dataset types</li>
 *   <li>Supporting progress tracking for long-running download operations</li>
 *   <li>Handling both local and remote dataset sources</li>
 * </ul>
 *
 * <p>The service supports both offline and online modes of operation through
 * the useLocalDatasets parameter, allowing validation to proceed with cached
 * data when network access is limited or to force fresh downloads when
 * up-to-date data is required.</p>
 *
 * <p>Dataset access is type-safe through generic methods that return strongly-typed
 * dataset objects based on the requested class type. This ensures compile-time
 * safety and eliminates casting requirements for client code.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * RDAPDatasetService service = // ... obtain service instance
 * boolean success = service.download(false, progressCallback);
 * if (success) {
 *     SomeDatasetType dataset = service.get(SomeDatasetType.class);
 *     // Use dataset for validation
 * }
 * </pre>
 *
 * @see ProgressCallback
 * @since 1.0.0
 */
public interface RDAPDatasetService {

  String DATASET_PATH = "datasets";

  /**
   * Downloads all required RDAP datasets for validation operations.
   *
   * <p>This convenience method calls the overloaded version with a null progress callback.
   * It downloads all necessary RDAP datasets based on the useLocalDatasets parameter,
   * either using cached local versions or fetching fresh data from remote sources.</p>
   *
   * @param useLocalDatasets whether to use cached local datasets instead of downloading fresh data
   * @return true if all datasets were successfully downloaded or loaded, false otherwise
   */
  boolean download(boolean useLocalDatasets);

  /**
   * Downloads all required RDAP datasets with progress tracking support.
   *
   * <p>This method downloads all necessary RDAP datasets for validation operations,
   * providing progress updates through the optional callback. The download behavior
   * is controlled by the useLocalDatasets parameter - when true, cached local
   * datasets are used if available; when false, fresh data is downloaded from
   * remote sources.</p>
   *
   * <p>Progress tracking allows long-running download operations to provide
   * feedback to users about the current status and completion percentage.
   * The callback is invoked periodically during the download process.</p>
   *
   * @param useLocalDatasets whether to use cached local datasets instead of downloading fresh data
   * @param progressCallback optional callback to receive progress updates during download,
   *                        may be null to disable progress tracking
   * @return true if all datasets were successfully downloaded or loaded, false otherwise
   */
  boolean download(boolean useLocalDatasets, ProgressCallback progressCallback);

  /**
   * Retrieves a dataset of the specified type from the loaded datasets.
   *
   * <p>This method provides type-safe access to loaded RDAP datasets based on
   * the requested class type. The generic return type eliminates the need for
   * casting and ensures compile-time type safety. Datasets must be previously
   * loaded through a successful download operation.</p>
   *
   * <p>The method uses the class type as a key to locate and return the
   * appropriate dataset instance. This allows for clean, type-safe access
   * to different types of RDAP validation data.</p>
   *
   * @param <T> the type of dataset to retrieve
   * @param clazz the Class object representing the dataset type to retrieve
   * @return the dataset instance of the specified type, or null if not found
   * @throws IllegalStateException if datasets have not been successfully downloaded
   */
  <T> T get(Class<T> clazz);
}
