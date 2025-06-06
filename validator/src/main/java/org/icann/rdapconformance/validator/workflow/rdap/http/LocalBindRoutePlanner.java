package org.icann.rdapconformance.validator.workflow.rdap.http;

// Place this class in an appropriate package
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.net.InetAddress;


public class LocalBindRoutePlanner implements HttpRoutePlanner {
    private final InetAddress localAddress;

    public LocalBindRoutePlanner(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public HttpRoute determineRoute(HttpHost host, HttpContext context) {
        System.out.println("[ROUTE] Determining route for host: " + host);
        System.out.println("[ROUTE] Local address: " + localAddress);
        System.out.println("[ROUTE] Context: " + context);
        boolean secure = "[ROUTE]  https".equalsIgnoreCase(host.getSchemeName());
        return new HttpRoute(host, localAddress, secure);
    }
}