package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.assertj.core.api.Assertions.assertThat;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.TIMEOUT_IN_5SECS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.schemavalidator.RDAPDatasetServiceMock;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;

public class TigValidation1Dot3Test {

  private RDAPValidatorConfiguration config;
  private HttpResponse<String> httpResponse;
  private QueryContext queryContext;

  @BeforeEach
  public void setUp() {
    config = mock(RDAPValidatorConfiguration.class);
    httpResponse = mock(HttpResponse.class);
    doReturn(Optional.empty()).when(httpResponse).previousResponse();
    RDAPQuery query = mock(RDAPQuery.class);
    doReturn(httpResponse).when(query).getRawResponse();
    // config.getUri() is stubbed per test, after the context is built, to avoid DNS warm-up
    queryContext = QueryContext.forTesting(config, new RDAPDatasetServiceMock(), query);
  }

  private void givenUri(String uri) {
    doReturn(URI.create(uri)).when(config).getUri();
    doReturn(config.getUri()).when(httpResponse).uri();
  }

  private static int closedPort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  @Test
  public void getGroupName() {
    givenUri("http://example.com/test");
    assertThat(new TigValidation1Dot3(queryContext).getGroupName()).isEqualTo("tigSection_1_3_Validation");
  }

  @Test
  public void testValidate_HttpScheme_NoSslCheck_IsOk() {
    givenUri("http://example.com/test");

    assertThat(new TigValidation1Dot3(queryContext).validate()).isTrue();
    assertThat(queryContext.getResults().getAll()).isEmpty();
  }

  @Test
  public void testValidate_HttpsConnectionRefused_ReturnsFalseWithoutResult() throws IOException {
    givenUri("https://127.0.0.1:" + closedPort() + "/test");

    assertThat(new TigValidation1Dot3(queryContext).validate()).isFalse();
    assertThat(queryContext.getResults().getAll()).isEmpty();
  }

  @Test
  public void testValidate_HttpsWithoutExplicitPort_UsesPort443() throws IOException {
    givenUri("https://rdap.example.com/test");
    ArgumentCaptor<SocketAddress> address = ArgumentCaptor.forClass(SocketAddress.class);

    try (MockedConstruction<Socket> sockets = mockConstruction(Socket.class, (socket, ctx) ->
        doThrow(new ConnectException("refused")).when(socket).connect(any(SocketAddress.class), anyInt()))) {

      assertThat(new TigValidation1Dot3(queryContext).validate()).isFalse();

      assertThat(sockets.constructed()).hasSize(1);
      verify(sockets.constructed().get(0)).connect(address.capture(), eq(TIMEOUT_IN_5SECS));
    }
    assertThat(((InetSocketAddress) address.getValue()).getPort()).isEqualTo(HTTPS_PORT);
    assertThat(((InetSocketAddress) address.getValue()).getHostString()).isEqualTo("rdap.example.com");
    assertThat(queryContext.getResults().getAll()).isEmpty();
  }

  @Test
  public void testValidate_HttpsHandshakeAgainstPlainServer_ReturnsFalse() throws IOException {
    try (ServerSocket plainServer = new ServerSocket(0)) {
      Thread responder = new Thread(() -> {
        try (var client = plainServer.accept()) {
          client.getOutputStream().write("not tls\n".getBytes());
          client.getOutputStream().flush();
        } catch (IOException ignored) {
          // server closed
        }
      });
      responder.setDaemon(true);
      responder.start();
      givenUri("https://127.0.0.1:" + plainServer.getLocalPort() + "/test");

      assertThat(new TigValidation1Dot3(queryContext).validate()).isFalse();
      assertThat(queryContext.getResults().getAll()).isEmpty();
    }
  }

  @Test
  public void testValidate_FollowsPreviousResponses() {
    HttpResponse<String> previous = mock(HttpResponse.class);
    doReturn(URI.create("http://example.com/redirected")).when(previous).uri();
    doReturn(Optional.empty()).when(previous).previousResponse();
    doReturn(Optional.of(previous)).when(httpResponse).previousResponse();
    givenUri("http://example.com/test");

    assertThat(new TigValidation1Dot3(queryContext).validate()).isTrue();
  }
}
