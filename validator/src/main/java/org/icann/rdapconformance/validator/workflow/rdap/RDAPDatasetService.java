package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.BootstrapDomainNameSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.DNSSecAlgNumbersDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.DsRrTypesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.EPPRoidDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.IPv4AddressSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.IPv6AddressSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.LinkRelationsDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.MediaTypesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.RDAPDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.RDAPExtensionsDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.RDAPJsonValuesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.RegistrarIdDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.SpecialIPv4AddressesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.datasets.SpecialIPv6AddressesDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPDatasetService {

  public final static String DATASET_PATH = "datasets";
  private static final Logger logger = LoggerFactory.getLogger(RDAPDatasetService.class);
  private final FileSystem fileSystem;
  private final Map<String, RDAPDataset> datasets;

  public RDAPDatasetService(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
    this.datasets = List.of(new IPv4AddressSpaceDataset(fileSystem),
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
        new EPPRoidDataset(fileSystem))
        .stream()
        .collect(Collectors.toMap(RDAPDataset::getName, Function.identity()));
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
    }
    return true;
  }

  public Optional<RDAPDataset> get(String name) {
    return Optional.ofNullable(this.datasets.get(name));
  }
}
