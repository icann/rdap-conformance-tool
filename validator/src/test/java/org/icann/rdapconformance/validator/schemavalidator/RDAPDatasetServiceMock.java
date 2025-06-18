package org.icann.rdapconformance.validator.schemavalidator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.*;

public class RDAPDatasetServiceMock implements RDAPDatasetService {

  private Map<Class<?>, Object> datasetValidatorModels;

  public RDAPDatasetServiceMock() {
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
                                          mock(RoleJsonValues.class),
                                          mock(EPPRoid.class)
                                      ).stream()
                                      .peek(mock -> doReturn(false).when(mock).isInvalid(any()))
                                      .collect(Collectors.toMap(Object::getClass, Function.identity()));

    RegistrarId registrarId = mock(RegistrarId.class);
    doReturn(true).when(registrarId).containsId(anyInt());
    doReturn(RegistrarIdTest.getValidRecord()).when(registrarId).getById(anyInt());
    this.datasetValidatorModels.put(registrarId.getClass(), registrarId);
  }

  public boolean download(boolean useLocalDatasets) {
    return true;
  }

  /**
   * Special handling for mock (mocked classes are in fact artificial subclasses and do not work
   * with basic equality)
   */
  public <T> T get(Class<T> clazz) {
    return (T) datasetValidatorModels.get(mock(clazz).getClass());
  }

  /**
   * Setup this mock as the instance to be returned by RDAPDatasetServiceImpl.getInstance()
   */
  public static void setupMock() {
    try {
      // Use reflection to access the private instance field
      java.lang.reflect.Field instanceField = RDAPDatasetServiceImpl.class.getDeclaredField("instance");
      instanceField.setAccessible(true);

      // Create a proxy that delegates to our mock
      RDAPDatasetServiceMock mock = new RDAPDatasetServiceMock();
      RDAPDatasetService proxy = (RDAPDatasetService) java.lang.reflect.Proxy.newProxyInstance(
          RDAPDatasetServiceImpl.class.getClassLoader(),
          new Class<?>[] { RDAPDatasetService.class },
          (proxy1, method, args) -> method.invoke(mock, args)
      );

      // Set the instance field to our proxy
      instanceField.set(null, proxy);
    } catch (Exception e) {
      throw new RuntimeException("Failed to setup RDAPDatasetServiceMock", e);
    }
  }
}