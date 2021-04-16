package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;

public class RDAPDatasetServiceMock extends RDAPDatasetService {

  private final Ipv4AddressSpace ipv4AddressSpaceMock;
  private final SpecialIPv4Addresses specialIPv4AddressesMock;

  public RDAPDatasetServiceMock() {
    super(mock(FileSystem.class));
    this.ipv4AddressSpaceMock = mock(Ipv4AddressSpace.class);
    this.specialIPv4AddressesMock = mock(SpecialIPv4Addresses.class);
    doReturn(false).when(ipv4AddressSpaceMock).isInvalid(any());
    doReturn(false).when(specialIPv4AddressesMock).isInvalid(any());
  }

  @Override
  public boolean download(boolean useLocalDatasets) {
    return true;
  }

  @Override
  public Ipv4AddressSpace getIpv4AddressSpaceMock() {
    return ipv4AddressSpaceMock;
  }

  @Override
  public SpecialIPv4Addresses getSpecialIPv4Addresses() {
    return specialIPv4AddressesMock;
  }
}
