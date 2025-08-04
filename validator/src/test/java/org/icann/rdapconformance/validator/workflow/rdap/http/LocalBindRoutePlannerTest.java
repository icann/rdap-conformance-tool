package org.icann.rdapconformance.validator.workflow.rdap.http;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

public class LocalBindRoutePlannerTest {

    private InetAddress mockLocalAddress;
    private HttpContext mockContext;

    @BeforeMethod
    public void setUp() throws Exception {
        mockLocalAddress = InetAddress.getLoopbackAddress();
        mockContext = mock(HttpContext.class);
    }

    @Test
    public void testConstructor_ValidLocalAddress() {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        
        assertThat(planner).isNotNull();
    }

    @Test
    public void testConstructor_NullLocalAddress() {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(null);
        
        assertThat(planner).isNotNull();
    }

    @Test
    public void testDetermineRoute_HttpsTarget() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("https", "example.com", 443);
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_HttpTarget() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("http", "example.com", 80);
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isFalse();
    }

    @Test
    public void testDetermineRoute_TargetWithoutScheme() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("example.com", 443);
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost().getHostName()).isEqualTo("example.com");
        assertThat(route.getTargetHost().getPort()).isEqualTo(443);
        assertThat(route.getTargetHost().getSchemeName()).isEqualTo("https");
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_NullLocalAddress() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(null);
        HttpHost target = new HttpHost("https", "example.com", 443);
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isNull();
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_DefaultHttpsPort() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("https", "example.com");
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_DefaultHttpPort() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("http", "example.com");
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isFalse();
    }

    @Test
    public void testDetermineRoute_CustomPort() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("https", "example.com", 8443);
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_MixedCaseScheme() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("HTTPS", "example.com", 443);
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_NullContext() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        HttpHost target = new HttpHost("https", "example.com", 443);
        
        HttpRoute route = planner.determineRoute(target, null);
        
        assertThat(route).isNotNull();
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getLocalAddress()).isEqualTo(mockLocalAddress);
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_DifferentLocalAddresses() throws Exception {
        InetAddress localAddress1 = InetAddress.getLoopbackAddress();
        InetAddress localAddress2 = InetAddress.getByName("127.0.0.1");
        
        LocalBindRoutePlanner planner1 = new LocalBindRoutePlanner(localAddress1);
        LocalBindRoutePlanner planner2 = new LocalBindRoutePlanner(localAddress2);
        
        HttpHost target = new HttpHost("https", "example.com", 443);
        
        HttpRoute route1 = planner1.determineRoute(target, mockContext);
        HttpRoute route2 = planner2.determineRoute(target, mockContext);
        
        assertThat(route1.getLocalAddress()).isEqualTo(localAddress1);
        assertThat(route2.getLocalAddress()).isEqualTo(localAddress2);
    }

    @Test
    public void testDetermineRoute_IPv6LocalAddress() throws Exception {
        InetAddress ipv6LocalAddress = InetAddress.getByName("::1");
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(ipv6LocalAddress);
        HttpHost target = new HttpHost("https", "example.com", 443);
        
        HttpRoute route = planner.determineRoute(target, mockContext);
        
        assertThat(route).isNotNull();
        assertThat(route.getLocalAddress()).isEqualTo(ipv6LocalAddress);
        assertThat(route.isSecure()).isTrue();
    }

    @Test
    public void testDetermineRoute_SchemeDetection() throws Exception {
        LocalBindRoutePlanner planner = new LocalBindRoutePlanner(mockLocalAddress);
        
        // Test various scheme cases
        HttpHost httpsTarget = new HttpHost("https", "example.com", 443);
        HttpHost httpTarget = new HttpHost("http", "example.com", 80);
        HttpHost ftpTarget = new HttpHost("ftp", "example.com", 21);
        
        HttpRoute httpsRoute = planner.determineRoute(httpsTarget, mockContext);
        HttpRoute httpRoute = planner.determineRoute(httpTarget, mockContext);
        HttpRoute ftpRoute = planner.determineRoute(ftpTarget, mockContext);
        
        assertThat(httpsRoute.isSecure()).isTrue();
        assertThat(httpRoute.isSecure()).isFalse();
        assertThat(ftpRoute.isSecure()).isFalse();
    }
}