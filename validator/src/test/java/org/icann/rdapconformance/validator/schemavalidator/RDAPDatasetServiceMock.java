package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;

public class RDAPDatasetServiceMock extends RDAPDatasetService {

  private final Ipv4AddressSpace ipv4AddressSpaceMock;
  private final SpecialIPv4Addresses specialIPv4AddressesMock;
  private final Ipv6AddressSpace ipv6AddressSpaceMock;
  private final SpecialIPv6Addresses specialIPv6AddressesMock;
  private final RDAPExtensions rdapExtensionsMock;
  private final LinkRelations linkRelationsMock;

  public RDAPDatasetServiceMock() {
    super(mock(FileSystem.class));
    this.ipv4AddressSpaceMock = mock(Ipv4AddressSpace.class);
    this.specialIPv4AddressesMock = mock(SpecialIPv4Addresses.class);
    doReturn(false).when(ipv4AddressSpaceMock).isInvalid(any());
    doReturn(false).when(specialIPv4AddressesMock).isInvalid(any());

    this.ipv6AddressSpaceMock = mock(Ipv6AddressSpace.class);
    this.specialIPv6AddressesMock = mock(SpecialIPv6Addresses.class);
    doReturn(false).when(ipv6AddressSpaceMock).isInvalid(any());
    doReturn(false).when(specialIPv6AddressesMock).isInvalid(any());

    this.rdapExtensionsMock = mock(RDAPExtensions.class);
    doReturn(false).when(rdapExtensionsMock).isInvalid(any());

    this.linkRelationsMock = mock(LinkRelations.class);
    doReturn(false).when(linkRelationsMock).isInvalid(any());
  }

  @Override
  public boolean download(boolean useLocalDatasets) {
    return true;
  }

  @Override
  public Ipv4AddressSpace getIpv4AddressSpace() {
    return ipv4AddressSpaceMock;
  }

  @Override
  public SpecialIPv4Addresses getSpecialIPv4Addresses() {
    return specialIPv4AddressesMock;
  }

  @Override
  public Ipv6AddressSpace getIpv6AddressSpace() {
    return ipv6AddressSpaceMock;
  }

  @Override
  public SpecialIPv6Addresses getSpecialIPv6Addresses() {
    return specialIPv6AddressesMock;
  }

  @Override
  public RDAPExtensions getRdapExtensions() {
    return rdapExtensionsMock;
  }

  @Override
  public LinkRelations getLinkRelations() {
    return linkRelationsMock;
  }
}
