package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TigValidation1Dot5_2024 extends ProfileValidation {

    private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot5_2024.class);
    private final HttpResponse<String> rdapResponse;
    private final RDAPValidatorConfiguration config;
    private final SSLValidator sslValidator;
    private final QueryContext queryContext;

    public TigValidation1Dot5_2024(QueryContext queryContext) {
        this(queryContext, new DefaultSSLValidator());
    }

    // Constructor for testing with injectable SSLValidator
    public TigValidation1Dot5_2024(QueryContext queryContext, SSLValidator sslValidator) {
        super(queryContext.getResults());
        this.queryContext = queryContext;
        this.rdapResponse = (HttpResponse<String>) queryContext.getQuery().getRawResponse();
        this.config = queryContext.getConfig();
        this.sslValidator = sslValidator;
    }

    /**
     * @deprecated Use TigValidation1Dot5_2024(QueryContext) or TigValidation1Dot5_2024(QueryContext, SSLValidator) instead
     * TODO: Migrate tests to QueryContext-only constructor
     */
    @Deprecated
    public TigValidation1Dot5_2024(HttpResponse<String> rdapResponse, RDAPValidatorConfiguration config, RDAPValidatorResults results, SSLValidator sslValidator) {
        super(results);
        this.queryContext = null; // Deprecated constructor - QueryContext not available
        this.rdapResponse = rdapResponse;
        this.config = config;
        this.sslValidator = sslValidator;
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
                int port = config.getUri().getPort();
                if (port < ZERO) {
                    port = HTTPS_PORT;
                }
                
                String hostname = config.getUri().getHost();
                
                // Use the SSLValidator to perform the SSL validation
                SSLValidator.SSLValidationResult sslResult = sslValidator.validateSSL(hostname, port);
                
                if (!sslResult.isSuccessful()) {
                    logger.info("SSL validation failed: {}", sslResult.getErrorMessage());
                    return false;
                }
                
                List<String> enabledProtocols = sslResult.getEnabledProtocols();
                
                // Validate TLS protocols
                for (String enabledProtocol : enabledProtocols) {
                    if (!"TLSv1.2".equalsIgnoreCase(enabledProtocol) && !"TLSv1.3".equalsIgnoreCase(enabledProtocol)) {
                        RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                            .code(-61100)
                            .value(response.uri().toString())
                            .message("The RDAP server must only use TLS 1.2 or TLS 1.3");

                        // Let QueryContext populate all HTTP fields including httpMethod and receivedHttpStatusCode
                        results.add(builder.build(queryContext));
                        isValid = false;
                    }
                }
                
                // Validate TLS 1.2 cipher suites using the SSLValidator
                if (enabledProtocols.contains("TLSv1.2")) {
                    SSLValidator.CipherValidationResult cipherResult = sslValidator.validateTLS12CipherSuites(hostname, port);
                    if (cipherResult != null && cipherResult.isSuccessful()) {
                        String cipher = cipherResult.getCipherSuite();
                        if (!isValidTLS12Cipher(cipher)) {
                            RDAPValidationResult.Builder builder = RDAPValidationResult.builder()
                                .code(-61101)
                                .value(response.uri().toString())
                                .message("The RDAP server must use one of the following cipher suites when using TLS 1.2: "
                                    + "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, "
                                    + "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256, TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384.");

                            // Let QueryContext populate all HTTP fields including httpMethod and receivedHttpStatusCode
                            results.add(builder.build(queryContext));
                            isValid = false;
                        }
                    } else if (cipherResult != null) {
                        logger.info("Cannot validate TLS 1.2 cipher suites: {}", cipherResult.getErrorMessage());
                        isValid = false;
                    } else {
                        logger.info("Cannot validate TLS 1.2 cipher suites: SSL validator returned null result");
                        isValid = false;
                    }
                }
                
            } // end of if https
            responseOpt = response.previousResponse();
        }
        return isValid;
    }
    
    private boolean isValidTLS12Cipher(String cipher) {
        return "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256".equalsIgnoreCase(cipher)
            || "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384".equalsIgnoreCase(cipher)
            || "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256".equalsIgnoreCase(cipher)
            || "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384".equalsIgnoreCase(cipher);
    }
}
