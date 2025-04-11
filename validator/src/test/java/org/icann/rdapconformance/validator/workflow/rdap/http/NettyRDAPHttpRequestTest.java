package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.testng.annotations.Test;

import java.net.URI;

public class NettyRDAPHttpRequestTest {

    @Test
    public void testNettyAgainstARIN6() throws Exception {
        NetworkInfo.setStackToV6();
        HttpResponse<String> response =  RDAPHttpRequest.makeHttpGetRequest(URI.create("https://rdap.arin.net/registry/entity/GOGL"), 5);
        System.out.println(response.statusCode());
        System.out.println(response.body());
        assertThat(true);
    }

    @Test
    public void testNettyAgainstARIN4() throws Exception {
        NetworkInfo.setStackToV4();
        HttpResponse<String> response  = RDAPHttpRequest.makeHttpGetRequest(URI.create("https://rdap.arin.net/registry/entity/GOGL"), 5);
        System.out.println(response.statusCode());
        System.out.println(response.body());
        assertThat(true);
    }

    @Test
    public void foof() throws Exception {
        NetworkInfo.setStackToV4();
        HttpResponse<String> response = RDAPHttpRequest.makeHttpGetRequest(URI.create("https://rdap.arin.net/registry/entity/GOGL"), 5);
        System.out.println(response.statusCode());
        System.out.println(response.body());
        System.out.println(response.statusCode());
        System.out.println(response.body());

    }
}