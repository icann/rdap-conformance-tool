package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS_PORT;

import java.net.URI;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.DefaultSSLValidator;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.SSLValidator;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gTLD Profile v3 conformance: if the scheme of the URI being tested is HTTPS, the TLS
 * certificate presented by the RDAP server must not have expired. Emits -66009 with the URI
 * otherwise.
 *
 * <p>Only the leaf certificate's notAfter date is checked. Chain trust, hostname matching,
 * revocation and the not-yet-valid case are out of scope of this test case and remain
 * covered by the connection-level checks (-13011 .. -13019).
 */
public final class ResponseValidationGtldProfile3TlsCertificate extends ProfileValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidationGtldProfile3TlsCertificate.class);

    private final QueryContext queryContext;
    private final SSLValidator sslValidator;

    public ResponseValidationGtldProfile3TlsCertificate(QueryContext queryContext) {
        this(queryContext, new DefaultSSLValidator(queryContext.getNetworkProtocol(), queryContext.getDnsResolver()));
    }

    ResponseValidationGtldProfile3TlsCertificate(QueryContext queryContext, SSLValidator sslValidator) {
        super(queryContext.getResults());
        this.queryContext = queryContext;
        this.sslValidator = sslValidator;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3TlsCertificate_Validation";
    }

    @Override
    public boolean doLaunch() {
        URI uri = queryContext.getConfig().getUri();
        return uri != null && HTTPS.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    protected boolean doValidate() {
        URI uri = queryContext.getConfig().getUri();
        int port = uri.getPort() < 0 ? HTTPS_PORT : uri.getPort();

        SSLValidator.CertificateExpirationResult result =
            sslValidator.checkCertificateExpiration(uri.getHost(), port);

        if (!result.isSuccessful()) {
            // Cannot inspect the certificate: do not report an unverifiable failure.
            logger.debug("Cannot check TLS certificate expiration for {}: {}", uri, result.getErrorMessage());
            return true;
        }
        if (!result.isExpired()) {
            return true;
        }
        results.add(RDAPValidationResult.builder()
            .code(-66009)
            .value(uri.toString())
            .message("The TLS certificate has expired.")
            .build(queryContext));
        return false;
    }
}
