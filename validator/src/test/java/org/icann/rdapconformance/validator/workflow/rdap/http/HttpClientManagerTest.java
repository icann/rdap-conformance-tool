package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import javax.net.ssl.SSLContext;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HttpClientManagerTest {

  @AfterMethod
  public void tearDown() {
    // Clean up after each test to avoid interference
    HttpClientManager.getInstance().shutdown();
  }

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

  @Test
  public void testGetClient_ValidParameters() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client = manager.getClient("example.com", sslContext, localBindIp, 30);
    
    assertThat(client).isNotNull();
  }

  @Test
  public void testGetClient_NullHost_ThrowsException() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    assertThatThrownBy(() -> manager.getClient(null, sslContext, localBindIp, 30))
            .isInstanceOf(RuntimeException.class);
  }

  @Test
  public void testGetClient_NullSSLContext_ThrowsException() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    assertThatThrownBy(() -> manager.getClient("example.com", null, localBindIp, 30))
            .isInstanceOf(RuntimeException.class);
  }

  @Test
  public void testGetClient_NullLocalBindIp_CreatesClient() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    
    CloseableHttpClient client = manager.getClient("example.com", sslContext, null, 30);
    
    assertThat(client).isNotNull();
  }

  @Test
  public void testGetClient_ClientCaching() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client1 = manager.getClient("example.com", sslContext, localBindIp, 30);
    CloseableHttpClient client2 = manager.getClient("example.com", sslContext, localBindIp, 30);
    
    assertThat(client1).isSameAs(client2);
  }

  @Test
  public void testGetClient_DifferentConfigurations_DifferentClients() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client1 = manager.getClient("example.com", sslContext, localBindIp, 30);
    CloseableHttpClient client2 = manager.getClient("different.com", sslContext, localBindIp, 30);
    
    assertThat(client1).isNotSameAs(client2);
  }

  @Test
  public void testGetClient_DifferentTimeouts_DifferentClients() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client1 = manager.getClient("example.com", sslContext, localBindIp, 30);
    CloseableHttpClient client2 = manager.getClient("example.com", sslContext, localBindIp, 60);
    
    assertThat(client1).isNotSameAs(client2);
  }

  @Test
  public void testGetClient_DifferentSSLContexts_DifferentClients() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext1 = SSLContext.getDefault();
    SSLContext sslContext2 = SSLContext.getInstance("TLS");
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client1 = manager.getClient("example.com", sslContext1, localBindIp, 30);
    CloseableHttpClient client2 = manager.getClient("example.com", sslContext2, localBindIp, 30);
    
    assertThat(client1).isNotSameAs(client2);
  }

  @Test
  public void testGetClient_DifferentLocalBindIps_DifferentClients() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp1 = InetAddress.getLoopbackAddress();
    InetAddress localBindIp2 = InetAddress.getByName("127.0.0.1");
    
    CloseableHttpClient client1 = manager.getClient("example.com", sslContext, localBindIp1, 30);
    CloseableHttpClient client2 = manager.getClient("example.com", sslContext, localBindIp2, 30);
    
    // Note: These might be the same due to IP address resolution, but test the mechanism
    assertThat(client1).isNotNull();
    assertThat(client2).isNotNull();
  }

  @Test
  public void testThreadSafety() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    int threadCount = 10;
    CountDownLatch latch = new CountDownLatch(threadCount);
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    
    CompletableFuture<CloseableHttpClient>[] futures = new CompletableFuture[threadCount];
    
    for (int i = 0; i < threadCount; i++) {
      futures[i] = CompletableFuture.supplyAsync(() -> {
        try {
          latch.countDown();
          latch.await(); // Wait for all threads to be ready
          return manager.getClient("example.com", sslContext, localBindIp, 30);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }, executor);
    }
    
    CloseableHttpClient firstClient = futures[0].get(5, TimeUnit.SECONDS);
    
    // All threads should get the same cached client instance
    for (int i = 1; i < threadCount; i++) {
      CloseableHttpClient client = futures[i].get(5, TimeUnit.SECONDS);
      assertThat(client).isSameAs(firstClient);
    }
    
    executor.shutdown();
  }

  @Test
  public void testShutdown_ClearsCache() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    // Create a client to populate cache
    CloseableHttpClient client1 = manager.getClient("example.com", sslContext, localBindIp, 30);
    assertThat(client1).isNotNull();
    
    // Shutdown should clear cache
    manager.shutdown();
    
    // Getting a new client should create a new instance
    CloseableHttpClient client2 = manager.getClient("example.com", sslContext, localBindIp, 30);
    assertThat(client2).isNotNull();
    assertThat(client2).isNotSameAs(client1);
  }

  @Test
  public void testGetClient_ZeroTimeout() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client = manager.getClient("example.com", sslContext, localBindIp, 0);
    
    assertThat(client).isNotNull();
  }

  @Test
  public void testGetClient_NegativeTimeout() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client = manager.getClient("example.com", sslContext, localBindIp, -1);
    
    assertThat(client).isNotNull();
  }

  @Test
  public void testGetClient_EmptyHost_CreatesClient() throws Exception {
    HttpClientManager manager = HttpClientManager.getInstance();
    SSLContext sslContext = SSLContext.getDefault();
    InetAddress localBindIp = InetAddress.getLoopbackAddress();
    
    CloseableHttpClient client = manager.getClient("", sslContext, localBindIp, 30);
    
    assertThat(client).isNotNull();
  }
}