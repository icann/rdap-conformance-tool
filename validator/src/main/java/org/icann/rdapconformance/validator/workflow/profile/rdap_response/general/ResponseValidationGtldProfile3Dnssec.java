package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Flags;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;
import org.xbill.DNS.dnssec.ValidatingResolver;

/**
 * gTLD Profile v3 conformance: the DNS resource records for the RDAP service
 * hostname must be DNSSEC signed and the DNSSEC chain of trust from the root
 * trust anchor to the hostname must be valid. Emits -66002 otherwise.
 */
public class ResponseValidationGtldProfile3Dnssec extends ProfileValidation {

    private static final Logger logger =
        LoggerFactory.getLogger(ResponseValidationGtldProfile3Dnssec.class);

    /**
     * IANA root zone trust anchors (KSK-2017 and KSK-2024) in dnsjava
     * master-file DS format. Source: https://data.iana.org/root-anchors/
     */
    static final String ROOT_TRUST_ANCHORS =
        ". IN DS 20326 8 2 E06D44B80B8F1D39A95C0B0D7C65D08458E880409BBC683457104237C7F8EC8D\n"
            + ". IN DS 38696 8 2 683D2D0ACB8C9B712A1948B27F741219298D0A450D612C483AF444A4C0FB2B16\n";

    private static final int DNS_TIMEOUT_SECONDS = 10;

    /**
     * Public DNSSEC-capable resolvers used as fallback when the configured
     * resolver does not pass through DNSSEC records (some local resolvers strip
     * or mangle RRSIGs, which would otherwise produce false -66002 findings).
     */
    static final String[] FALLBACK_DNSSEC_RESOLVERS = {"8.8.8.8", "1.1.1.1"};

    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3Dnssec(QueryContext queryContext) {
        super(queryContext.getResults());
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3DnssecChainOfTrust_Validation";
    }

    @Override
    protected boolean doValidate() {
        String host = queryContext.getConfig().getUri() == null
            ? null : queryContext.getConfig().getUri().getHost();

        if (host == null || host.isEmpty()) {
            logger.debug("No RDAP service hostname available, skipping -66002 check.");
            return true;
        }

        if (!isDnssecChainOfTrustValid(host)) {
            results.add(RDAPValidationResult.builder()
                .code(-66002)
                .value(host)
                .message("The DNS resource records for the RDAP service are not DNSSEC signed "
                    + "or the DNSSEC chain of trust is not valid.")
                .build(queryContext));
            return false;
        }

        return true;
    }

    /**
     * Resolves the A record of the hostname through a DNSSEC-validating resolver
     * anchored at the root zone. The chain of trust is valid only when the
     * resolver sets the AD (Authenticated Data) flag on a NOERROR response.
     *
     * <p>Each candidate upstream resolver is tried in order; the first one that
     * produces an authenticated answer makes the validation pass. This avoids
     * false positives when a local resolver strips or mangles DNSSEC records,
     * while a genuinely broken chain of trust fails against every upstream.</p>
     *
     * <p>Overridable for testing.</p>
     */
    boolean isDnssecChainOfTrustValid(String host) {
        for (Resolver upstream : createUpstreamResolvers()) {
            try {
                ValidatingResolver validatingResolver = new ValidatingResolver(upstream);
                validatingResolver.loadTrustAnchors(
                    new ByteArrayInputStream(ROOT_TRUST_ANCHORS.getBytes(StandardCharsets.UTF_8)));

                Name name = Name.fromString(host, Name.root);
                Message query = Message.newQuery(Record.newRecord(name, Type.A, DClass.IN));
                Message response = validatingResolver.send(query);

                int rcode = response.getHeader().getRcode();
                boolean authenticated = response.getHeader().getFlag(Flags.AD);
                logger.debug("DNSSEC validation for {} via {}: rcode={}, AD={}",
                    host, upstream, Rcode.string(rcode), authenticated);

                if (rcode == Rcode.NOERROR && authenticated) {
                    return true;
                }
            } catch (Exception e) {
                logger.info("DNSSEC chain of trust check errored for {}: {}", host, e.getMessage());
            }
        }
        return false;
    }

    /**
     * Candidate upstream (non-validating) resolvers used to fetch records and
     * DNSSEC material: first the tool's configured resolver (honors a custom
     * DNS server when one was supplied), then well-known public resolvers as
     * fallback. TCP is used on the fallbacks because DNSSEC responses commonly
     * exceed UDP limits. Overridable for testing.
     */
    List<Resolver> createUpstreamResolvers() {
        List<Resolver> resolvers = new ArrayList<>();
        try {
            Resolver configured = queryContext.getDnsResolver().getResolver();
            if (configured != null) {
                resolvers.add(configured);
            }
        } catch (Exception e) {
            logger.debug("Could not obtain the configured DNS resolver: {}", e.getMessage());
        }
        for (String server : FALLBACK_DNSSEC_RESOLVERS) {
            try {
                resolvers.add(newTcpResolver(server));
            } catch (Exception e) {
                logger.debug("Could not create DNS resolver for {}: {}", server, e.getMessage());
            }
        }
        return resolvers;
    }

    private static Resolver newTcpResolver(String server) throws Exception {
        SimpleResolver resolver = new SimpleResolver(server);
        resolver.setTCP(true);
        resolver.setTimeout(Duration.ofSeconds(DNS_TIMEOUT_SECONDS));
        return resolver;
    }
}
