package org.icann.rdapconformance.validator.workflow.rdap.http;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RDAPHttpRequest {

    private static final String GET = "GET";
    private static final String HEAD = "HEAD";
    private static final String ACCEPT = "Accept";
    private static final String RDAP_JSON_APPLICATION_JSON = "application/rdap+json, application/json";

    public static HttpResponse<String> makeHttpGetRequest(URI uri, int timeout)
        throws IOException, InterruptedException {
        return makeHttpRequest(uri, timeout, GET);
    }

    public static HttpResponse<String> makeHttpHeadRequest(URI uri, int timeout)
        throws IOException, InterruptedException {
        return makeHttpRequest(uri, timeout, HEAD);
    }

    private static HttpResponse<String> makeHttpRequest(URI uri, int timeout, String method)
        throws IOException, InterruptedException {

        HttpClient client = HttpClient.newBuilder()
                                      .connectTimeout(Duration.of(timeout, SECONDS))
                                      .followRedirects(HttpClient.Redirect.NEVER)
                                      .version(HttpClient.Version.HTTP_2)
                                      .build();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                                                        .uri(uri)
                                                        .timeout(Duration.of(timeout, SECONDS))
                                                        .header(ACCEPT, RDAP_JSON_APPLICATION_JSON);

        HttpRequest request = switch (method) {
            case GET -> requestBuilder.GET().build();
            case HEAD -> requestBuilder.method(HEAD, HttpRequest.BodyPublishers.noBody()).build();
            default -> throw new RuntimeException("Unsupported HTTP request method: " + method);
        };

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}