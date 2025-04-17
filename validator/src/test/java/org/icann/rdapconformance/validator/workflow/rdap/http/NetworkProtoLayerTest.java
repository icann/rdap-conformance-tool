package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import org.icann.rdapconformance.validator.NetworkInfo;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.net.URI;

public class NetworkProtoLayerTest {

    public static final String EXTERNAL_SITE = "https://rdap.arin.net/registry/entity/GOGL";
    public static int TIMEOUT = 15;

    // TODO: this test doens't work in GitHub actions because of the network restrictions?
    @Ignore
    @Test
    public void testProtoV6() throws Exception {
        NetworkInfo.setStackToV6();
        HttpResponse<String> response =  RDAPHttpRequest.makeHttpGetRequest(URI.create(EXTERNAL_SITE), TIMEOUT);
        System.out.println(response.statusCode());
        System.out.println(response.body());
        assertThat(true); // this isn't a true assertion, but a placeholder for something to test, but how?
    }

    // TODO: this test doesn't work in GitHub actions because of the network restrictions?
    @Ignore
    @Test
    public void testProtoV4() throws Exception {
        NetworkInfo.setStackToV4();
        HttpResponse<String> response  = RDAPHttpRequest.makeHttpGetRequest(URI.create(EXTERNAL_SITE), TIMEOUT);
        System.out.println(response.statusCode());
        System.out.println(response.body());
        assertThat(true); // this isn't a true assertion, but a placeholder for something to test, but how?
    }
}