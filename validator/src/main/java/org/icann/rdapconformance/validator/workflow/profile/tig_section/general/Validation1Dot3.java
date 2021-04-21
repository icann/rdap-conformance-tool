package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Validation1Dot3 {

  private static final Logger logger = LoggerFactory.getLogger(Validation1Dot3.class);

  public static boolean validate(RDAPValidatorConfiguration config,
      RDAPValidatorResults results) {
    if (config.getUri().getScheme().equals("https")) {
      try {
        SSLContext sslContext = SSLContext.getDefault();
        int port = config.getUri().getPort();
        if (port < 0) {
          port = 443;
        }
        List<String> enabledProtocols;
        try (SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory()
            .createSocket(config.getUri().getHost(), port)) {
          sslSocket.startHandshake();
          enabledProtocols = Arrays.asList(sslSocket.getEnabledProtocols());
        }
        if (enabledProtocols.contains("SSLv2") || enabledProtocols.contains("SSLv3")) {
          results.add(RDAPValidationResult.builder()
              .code(-20200)
              .value(config.getUri().toString())
              .message("The RDAP server is offering SSLv2 and/or SSLv3.")
              .build());
          return false;
        }
      } catch (NoSuchAlgorithmException | IOException e) {
        logger.error("Cannot create SSL context", e);
      }
    }
    return true;
  }
}
