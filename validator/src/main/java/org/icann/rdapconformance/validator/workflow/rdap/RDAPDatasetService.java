package org.icann.rdapconformance.validator.workflow.rdap;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.BootstrapDomainNameSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.DNSSecAlgNumbersDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.DsRrTypesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.EPPRoidDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.IPv4AddressSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.IPv6AddressSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.LinkRelationsDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.MediaTypesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.RDAPDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.RDAPExtensionsDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.RDAPJsonValuesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.RegistrarIdDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.SpecialIPv4AddressesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.SpecialIPv6AddressesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
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
      if (!dataset.parse()) {
        logger.error("Failed to parse dataset {}", dataset.getName());
        return false;
      }
    }
    return true;
  }

  private RDAPDataset get(String name) {
    return Optional.ofNullable(this.datasets.get(name))
        .orElseThrow(() ->
        new IllegalArgumentException("Can't find required dataset " + name)
    );
  }

  public Ipv4AddressSpace getIpv4AddressSpace() {
    return (Ipv4AddressSpace) get("ipv4AddressSpace").getData();
  }

  public SpecialIPv4Addresses getSpecialIPv4Addresses() {
    return (SpecialIPv4Addresses) get("specialIPv4Addresses").getData();
  }

  public Ipv6AddressSpace getIpv6AddressSpace() {
    return (Ipv6AddressSpace) get("ipv6AddressSpace").getData();
  }

  public SpecialIPv6Addresses getSpecialIPv6Addresses() {
    return (SpecialIPv6Addresses) get("specialIPv6Addresses").getData();
  }

  public RDAPExtensions getRdapExtensions() {
    return (RDAPExtensions) get("RDAPExtensions").getData();
  }

  public LinkRelations getLinkRelations() {
    return (LinkRelations) get("linkRelations").getData();
  }
}
