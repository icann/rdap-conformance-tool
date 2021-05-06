package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DsRrTypes;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EventActionJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.NoticeAndRemarkJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarName;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RoleJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.StatusJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.VariantRelationJsonValues;

public class RDAPDatasetServiceMock extends RDAPDatasetService {

  public RDAPDatasetServiceMock() {
    super(mock(FileSystem.class));
    this.datasetValidatorModels = List.of(
        mock(Ipv4AddressSpace.class),
        mock(SpecialIPv4Addresses.class),
        mock(Ipv6AddressSpace.class),
        mock(SpecialIPv6Addresses.class),
        mock(RDAPExtensions.class),
        mock(LinkRelations.class),
        mock(MediaTypes.class),
        mock(NoticeAndRemarkJsonValues.class),
        mock(EventActionJsonValues.class),
        mock(StatusJsonValues.class),
        mock(VariantRelationJsonValues.class),
        mock(RoleJsonValues.class)
    ).stream()
        .peek(mock -> doReturn(false).when(mock).isInvalid(any()))
        .collect(Collectors.toMap(Object::getClass, Function.identity()));

    RegistrarId registrarId = mock(RegistrarId.class);
    doReturn(true).when(registrarId).contains(anyInt());
    this.datasetValidatorModels.put(registrarId.getClass(), registrarId);
    RegistrarName registrarName = mock(RegistrarName.class);
    this.datasetValidatorModels.put(registrarName.getClass(), registrarName);
  }

  @Override
  public boolean download(boolean useLocalDatasets) {
    return true;
  }

  /**
   * Special handling for mock (mocked classes are in fact artificial subclasses and do not work
   * with basic equality)
   */
  @Override
  public <T> T get(Class<T> clazz) {
    return (T) datasetValidatorModels.get(mock(clazz).getClass());
  }
}
