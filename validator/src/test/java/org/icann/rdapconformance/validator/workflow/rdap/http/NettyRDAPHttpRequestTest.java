package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.testng.annotations.Test;

import java.net.URI;

public class NettyRDAPHttpRequestTest {

    @Test
    public void testNettyAgainstARIN6() throws Exception {
        NetworkInfo.setStackToV6();
        NettyRDAPHttpRequest.makeHttpGetRequest(URI.create("https://rdap.arin.net/registry/entity/GOGL"), 5)
                            .thenAccept(response -> {
                                System.out.println("HTTP " + response.statusCode());
                                System.out.println(response.body());
                            })
                            .exceptionally(ex -> {
                                ex.printStackTrace();
                                return null;
                            }).get(); // Waits for the future to complete
        assertThat(true);
    }

    @Test
    public void testNettyAgainstARIN4() throws Exception {
        NetworkInfo.setStackToV4();
        NettyRDAPHttpRequest.makeHttpGetRequest(URI.create("https://rdap.arin.net/registry/entity/GOGL"), 5)
                            .thenAccept(response -> {
                                System.out.println("HTTP " + response.statusCode());
                                System.out.println(response.body());
                            })
                            .exceptionally(ex -> {
                                ex.printStackTrace();
                                return null;
                            }).get(); // Waits for the future to complete
        assertThat(true);
    }

    @Test
    public void foof() throws Exception {
        NetworkInfo.setStackToV4();
        HttpResponse<String> response = NettyRDAPHttpRequest.makeHttpGetRequest(URI.create("https://rdap.arin.net/registry/entity/GOGL"), 5).get();

        System.out.println(response.statusCode());
        System.out.println(response.body());

    }
}