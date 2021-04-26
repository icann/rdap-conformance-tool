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

  public static HttpResponse<String> makeHttpGetRequest(URI uri, int timeout)
      throws IOException, InterruptedException {
    return makeHttpGetRequest(uri, timeout, "GET");
  }

  public static HttpResponse<String> makeHttpHeadRequest(URI uri, int timeout)
      throws IOException, InterruptedException {
    return makeHttpGetRequest(uri, timeout, "HEAD");
  }

  private static HttpResponse<String> makeHttpGetRequest(URI uri, int timeout, String method)
      throws IOException, InterruptedException {
    HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
        .uri(uri)
        .version(Version.HTTP_2)
        .timeout(Duration.of(timeout, SECONDS));
    HttpRequest request;
    switch (method) {
      case "GET":
        request = httpRequestBuilder
            .GET()
            .build();
        break;
      case "HEAD":
        request = httpRequestBuilder
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .build();
        break;
      default:
        throw new RuntimeException("Unsupported HTTP request method " + method);
    }
    return HttpClient.newBuilder()
        .connectTimeout(Duration.of(timeout, SECONDS))
        .followRedirects(Redirect.ALWAYS)
        .build()
        .send(request, HttpResponse.BodyHandlers.ofString());
  }
}
