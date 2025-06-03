package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.TIMEOUT_IN_5SECS;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TigValidation1Dot3 extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot3.class);
  public static final String SS_LV_2 = "SSLv2";
  public static final String SS_LV_3 = "SSLv3";
  private final HttpResponse<String> rdapResponse;
  private final RDAPValidatorConfiguration config;

  public TigValidation1Dot3(HttpResponse<String> rdapResponse, RDAPValidatorConfiguration config,
      RDAPValidatorResults results) {
    super(results);
    this.rdapResponse = rdapResponse;
    this.config = config;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_3_Validation";
  }
  @Override
  public boolean doValidate() {
    boolean isValid = true;
    Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
    while (responseOpt.isPresent()) {
      HttpResponse<String> response = responseOpt.get();
      if (response.uri().getScheme().equals(HTTPS)) {
        try {
          SSLContext sslContext = SSLContext.getDefault();
          int port = config.getUri().getPort();
          if (port < ZERO) {
            port = HTTPS_PORT;
          }
          List<String> enabledProtocols;
          try (Socket socket = new Socket()) {
            // Set a timeout for the connection
            int timeoutMillis = TIMEOUT_IN_5SECS;
            socket.connect(new InetSocketAddress(config.getUri().getHost(), port), timeoutMillis);

            // Wrap the socket in an SSLSocket
            try (SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(socket,
                config.getUri().getHost(), port, true)) {
              sslSocket.startHandshake();
              enabledProtocols = Arrays.asList(sslSocket.getEnabledProtocols());
              logger.debug("Enabled protocols: {}", enabledProtocols);
            }
          }
          if (enabledProtocols.contains(SS_LV_2) || enabledProtocols.contains(SS_LV_3)) {
            results.add(RDAPValidationResult.builder()
                                            .code(-20200)
                                            .value(response.uri().toString())
                                            .message("The RDAP server is offering SSLv2 and/or SSLv3.")
                                            .build());
            isValid = false;
          }
        } catch (NoSuchAlgorithmException | IOException e) {
          logger.info("Cannot create SSL context or connect to the server", e);
          return false; // Return false if an exception occurs
        }
      }
      responseOpt = response.previousResponse();
    }
    return isValid;
  }
}
