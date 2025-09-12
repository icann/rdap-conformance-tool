package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.ProgressCallback;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.*;

public class RDAPDatasetServiceTestMock implements RDAPDatasetService {

  private Map<Class<?>, Object> datasetValidatorModels;
  private Set<String> invalidEPPROIDs;

  public RDAPDatasetServiceTestMock() {
    this(Set.of()); // Default: no invalid EPPROIDs
  }
  
  public RDAPDatasetServiceTestMock(Set<String> invalidEPPROIDs) {
    this.invalidEPPROIDs = invalidEPPROIDs;
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

    // Create custom EPPRoid mock with configurable invalid ROIDs
    EPPRoid eppRoid = mock(EPPRoid.class);
    // Set up the mock to return true for invalid EPPROIDs, false otherwise
    doReturn(false).when(eppRoid).isInvalid(any()); // Default to false
    for (String invalidRoid : this.invalidEPPROIDs) {
      doReturn(true).when(eppRoid).isInvalid(invalidRoid);
    }
    this.datasetValidatorModels.put(eppRoid.getClass(), eppRoid);

    RegistrarId registrarId = mock(RegistrarId.class);
    doReturn(true).when(registrarId).containsId(anyInt());
    doReturn(RegistrarIdTest.getValidRecord()).when(registrarId).getById(anyInt());
    this.datasetValidatorModels.put(registrarId.getClass(), registrarId);
  }

  public boolean download(boolean useLocalDatasets) {
    return true;
  }

  @Override
  public boolean download(boolean useLocalDatasets, ProgressCallback progressCallback) {
    return true;
  }

    /**
     * Uses a temp mock to lookup the right class in our dataset map
     */
  public <T> T get(Class<T> clazz) {
    return (T) datasetValidatorModels.get(mock(clazz).getClass());
  }
}