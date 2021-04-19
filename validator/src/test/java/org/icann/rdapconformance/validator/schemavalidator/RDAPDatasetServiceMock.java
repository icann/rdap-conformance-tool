package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.NoticeAndRemarkJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;

public class RDAPDatasetServiceMock extends RDAPDatasetService {

  public RDAPDatasetServiceMock() {
    super(mock(FileSystem.class));
    Ipv4AddressSpace ipv4AddressSpaceMock = mock(Ipv4AddressSpace.class);
    SpecialIPv4Addresses specialIPv4AddressesMock = mock(SpecialIPv4Addresses.class);
    doReturn(false).when(ipv4AddressSpaceMock).isInvalid(any());
    doReturn(false).when(specialIPv4AddressesMock).isInvalid(any());

    Ipv6AddressSpace ipv6AddressSpaceMock = mock(Ipv6AddressSpace.class);
    SpecialIPv6Addresses specialIPv6AddressesMock = mock(SpecialIPv6Addresses.class);
    doReturn(false).when(ipv6AddressSpaceMock).isInvalid(any());
    doReturn(false).when(specialIPv6AddressesMock).isInvalid(any());

    RDAPExtensions rdapExtensionsMock = mock(RDAPExtensions.class);
    doReturn(false).when(rdapExtensionsMock).isInvalid(any());

    LinkRelations linkRelationsMock = mock(LinkRelations.class);
    doReturn(false).when(linkRelationsMock).isInvalid(any());

    MediaTypes mediaTypesMock = mock(MediaTypes.class);
    doReturn(false).when(mediaTypesMock).isInvalid(any());

    NoticeAndRemarkJsonValues noticeAndRemarkJsonValues = mock(NoticeAndRemarkJsonValues.class);
    doReturn(false).when(noticeAndRemarkJsonValues).isInvalid(any());

    this.datasetValidatorModels = List.of(
        ipv4AddressSpaceMock,
        specialIPv4AddressesMock,
        ipv6AddressSpaceMock,
        specialIPv6AddressesMock,
        rdapExtensionsMock,
        linkRelationsMock,
        mediaTypesMock,
        noticeAndRemarkJsonValues
    ).stream()
        .collect(Collectors.toMap(Object::getClass, Function.identity()));
  }

  @Override
  public boolean download(boolean useLocalDatasets) {
    return true;
  }

  @Override
  public <T> T get(Class<T> clazz) {
    return (T) datasetValidatorModels.get(mock(clazz).getClass());
  }
}
