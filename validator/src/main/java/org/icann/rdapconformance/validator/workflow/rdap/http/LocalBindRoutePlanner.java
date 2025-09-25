package org.icann.rdapconformance.validator.workflow.rdap.http;

import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.net.InetAddress;

public class LocalBindRoutePlanner implements HttpRoutePlanner {
    private final InetAddress localAddress;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LocalBindRoutePlanner.class);

    public LocalBindRoutePlanner(InetAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public HttpRoute determineRoute(HttpHost target, HttpContext context) throws HttpException {
        logger.debug("[ROUTE] Determining route for host: " + target + " with local address: " + localAddress);

        // Create a proper HttpHost from the target if needed
        HttpHost host = target;
        if (target.getSchemeName() == null) {
            host = new HttpHost("https", target.getHostName(), target.getPort());
        }

        boolean secure = "https".equalsIgnoreCase(host.getSchemeName());
        return new HttpRoute(host, localAddress, secure);
    }
}