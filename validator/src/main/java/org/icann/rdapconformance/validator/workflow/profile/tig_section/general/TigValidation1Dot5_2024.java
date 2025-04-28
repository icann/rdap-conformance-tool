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
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.HTTPSRecord;

public class TigValidation1Dot5_2024 extends ProfileValidation {

    private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot5_2024.class);
    private final HttpResponse<String> rdapResponse;
    private final RDAPValidatorConfiguration config;

    public TigValidation1Dot5_2024(HttpResponse<String> rdapResponse, RDAPValidatorConfiguration config,
        RDAPValidatorResults results) {
        super(results);
        this.rdapResponse = rdapResponse;
        this.config = config;
    }

    @Override
    public String getGroupName() {
        return "tigSection_1_5_Validation";
    }

    @Override
    public boolean doValidate() {
        boolean isValid = true;

        Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
        while (responseOpt.isPresent()) {
            HttpResponse<String> response = responseOpt.get();
            if (response.uri().getScheme().equals(HTTPS)) {
                SSLContext sslContext;

                try {
                    sslContext = SSLContext.getDefault();
                } catch (NoSuchAlgorithmException e) {
                    logger.info("Cannot create SSL context", e);
                    return false;
                }

                int port = config.getUri().getPort();
                if (port < ZERO) {
                    port = HTTPS_PORT;
                }

                List<String> enabledProtocols;
                try (Socket socket = new Socket()) {
                    socket.connect(new InetSocketAddress(config.getUri().getHost(), port), TIMEOUT_IN_5SECS);
                    try (SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(socket,
                        config.getUri().getHost(), port, true)) {
                        sslSocket.startHandshake();
                        enabledProtocols = Arrays.asList(sslSocket.getEnabledProtocols());
                        logger.debug("Enabled protocols: {}", enabledProtocols);
                    }
                } catch (IOException e) {
                    logger.info("Error during SSL connection setup", e);
                    return false;
                }

                for (String enabledProtocol : enabledProtocols) {
                    if (!"TLSv1.2".equalsIgnoreCase(enabledProtocol) && !"TLSv1.3".equalsIgnoreCase(enabledProtocol)) {
                        results.add(RDAPValidationResult.builder()
                            .code(-61100)
                            .value(response.uri().toString())
                            .message("The RDAP server must only use TLS 1.2 or TLS 1.3")
                            .build());
                        isValid = false;
                    }

                    if ("TLSv1.2".equalsIgnoreCase(enabledProtocol)) {
                        try (SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(config.getUri().getHost(), port)) {

                            sslSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
                            sslSocket.startHandshake();
                            SSLSession sslSession = sslSocket.getSession();

                            String protocol = sslSession.getProtocol();
                            String cipher = sslSession.getCipherSuite();
                            logger.info("cipher for protocol {} is {}", protocol, cipher);

                            if (!"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equalsIgnoreCase(cipher)
                                && !"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384".equalsIgnoreCase(cipher)
                                && !"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256".equalsIgnoreCase(cipher)
                                && !"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384".equalsIgnoreCase(cipher)) {

                                results.add(RDAPValidationResult.builder()
                                    .code(-61101)
                                    .value(response.uri().toString())
                                    .message("The RDAP server must use one of the following cipher suites when using TLS 1.2: "
                                        + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, "
                                        + "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384.")
                                    .build());
                                isValid = false;
                            }
                        } catch (IOException e) {
                            logger.info("Cannot create SSL connection", e);
                            return false;
                        }
                    } // end of TLSv1.2 protocol
                } // end of for each enabledProtocol
            } // end of if https
            responseOpt = response.previousResponse();
        }
        return isValid;
    }
}
