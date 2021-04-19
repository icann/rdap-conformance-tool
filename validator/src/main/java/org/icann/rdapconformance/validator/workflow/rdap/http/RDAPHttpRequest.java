package org.icann.rdapconformance.validator.workflow.rdap.http;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class RDAPHttpRequest {

  public static HttpResponse<String> makeHttpRequest(URI uri, int timeout)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .version(Version.HTTP_2)
        .timeout(Duration.of(timeout, SECONDS))
        .GET()
        .build();
    return HttpClient.newBuilder()
        .connectTimeout(Duration.of(timeout, SECONDS))
        .followRedirects(Redirect.ALWAYS)
        .build()
        .send(request, HttpResponse.BodyHandlers.ofString());
  }
}
