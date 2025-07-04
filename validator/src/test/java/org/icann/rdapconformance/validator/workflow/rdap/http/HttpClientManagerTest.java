package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class HttpClientManagerTest {

  @Test
  public void testGetInstanceReturnsSingleton() {
    HttpClientManager instance1 = HttpClientManager.getInstance();
    HttpClientManager instance2 = HttpClientManager.getInstance();
    
    assertThat(instance1).isSameAs(instance2);
  }

  @Test
  public void testInstanceIsNotNull() {
    HttpClientManager instance = HttpClientManager.getInstance();
    assertThat(instance).isNotNull();
  }

  @Test
  public void testShutdownDoesNotThrow() {
    HttpClientManager instance = HttpClientManager.getInstance();
    // Should not throw any exceptions
    instance.shutdown();
  }
}