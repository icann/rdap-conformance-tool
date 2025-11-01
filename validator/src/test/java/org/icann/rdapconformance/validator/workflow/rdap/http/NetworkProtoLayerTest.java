package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.NetworkProtocol;
import org.icann.rdapconformance.validator.QueryContext;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.net.URI;

public class NetworkProtoLayerTest {

    public static final String EXTERNAL_SITE = "https://rdap.arin.net/registry/entity/GOGL";
    public static int TIMEOUT = 15;

    // TODO: this test doesn't work in GitHub actions because of the network restrictions?
    @Ignore
    @Test
    public void testProtoV6() throws Exception {
        // Create QueryContext with IPv6 protocol
        QueryContext qctx = QueryContext.forTesting();
        qctx.setNetworkProtocol(NetworkProtocol.IPv6);
        qctx.setStackToV6();

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(qctx, URI.create(EXTERNAL_SITE), TIMEOUT, "GET");
        System.out.println(response.statusCode());
        System.out.println(response.body());
        assertThat(true); // this isn't a true assertion, but a placeholder for something to test, but how?
    }

    // TODO: this test doesn't work in GitHub actions because of the network restrictions?
    @Ignore
    @Test
    public void testProtoV4() throws Exception {
        // Create QueryContext with IPv4 protocol
        QueryContext qctx = QueryContext.forTesting();
        qctx.setNetworkProtocol(NetworkProtocol.IPv4);
        qctx.setStackToV4();

        HttpResponse<String> response = RDAPHttpRequest.makeRequest(qctx, URI.create(EXTERNAL_SITE), TIMEOUT, "GET");
        System.out.println(response.statusCode());
        System.out.println(response.body());
        assertThat(true); // this isn't a true assertion, but a placeholder for something to test, but how?
    }
}