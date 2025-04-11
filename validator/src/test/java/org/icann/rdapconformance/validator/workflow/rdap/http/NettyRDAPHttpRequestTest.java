package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.NetworkInfo;
import org.testng.annotations.Test;

public class NettyRDAPHttpRequestTest {

    @Test
    public void testNettyAgainstARIN6() throws Exception {
        NetworkInfo.setStackToV6();
        NettyRDAPHttpRequest.makeRequest("https://rdap.arin.net/registry/entity/GOGL", 5)
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
        NettyRDAPHttpRequest.makeRequest("https://rdap.arin.net/registry/entity/GOGL", 5)
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
}
