package org.icann.rdapconformance.validator;

import static org.mockito.Mockito.mock;

import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;

public class RDAPDatasetServiceMock extends RDAPDatasetService {

  public RDAPDatasetServiceMock() {
    super(new LocalFileSystem());
  }

  @Override
  public Ipv4AddressSpace getIpv4AddressSpace() {
    return mock(Ipv4AddressSpace.class);
  }

  @Override
  public SpecialIPv4Addresses getSpecialIPv4Addresses() {
    return mock(SpecialIPv4Addresses.class);
  }
}
