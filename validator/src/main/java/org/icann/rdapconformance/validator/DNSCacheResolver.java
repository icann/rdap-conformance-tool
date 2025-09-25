package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.DOT;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv6;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;
import static org.icann.rdapconformance.validator.CommonUtils.addErrorToResultsFile;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.xbill.DNS.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.xbill.DNS.Record;

/**
 * DNS resolution utility with caching capabilities for RDAP validation.
 *
 * <p>This class provides DNS resolution services with the following features:</p>
 * <ul>
 *   <li>Cached A and AAAA record lookups to improve performance</li>
 *   <li>IPv4 and IPv6 address resolution and validation</li>
 *   <li>Configurable timeouts and retry logic for DNS queries</li>
 *   <li>Support for both system-configured and custom DNS resolvers</li>
 *   <li>Thread-safe caching using ConcurrentHashMap</li>
 *   <li>Validation of IPv4/IPv6 address availability for target hosts</li>
 * </ul>
 *
 * <p>The resolver is initialized once using system DNS settings and configured with
 * a 10-second timeout and 3 retries to balance reliability with performance. Results
 * are cached in separate maps for IPv4 (A records) and IPv6 (AAAA records) to avoid
 * repeated DNS lookups during validation.</p>
 *
 * <p>This class is essential for RDAP validation as it determines which IP protocols
 * (IPv4/IPv6) are available for testing, and enables the tool to make informed
 * decisions about which network stacks to use for validation queries.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * DNSCacheResolver.initFromUrl("https://rdap.example.com/domain/test.com");
 * boolean hasV4 = DNSCacheResolver.hasV4Addresses("https://rdap.example.com/domain/test.com");
 * boolean hasV6 = DNSCacheResolver.hasV6Addresses("https://rdap.example.com/domain/test.com");
 * </pre>
 *
 * @see ExtendedResolver
 * @see InetAddress
 * @since 1.0.0
 */
public class DNSCacheResolver {

    private static final Logger logger = LoggerFactory.getLogger(DNSCacheResolver.class);

    private static final Map<String, List<InetAddress>> CACHE_V4 = new ConcurrentHashMap<>();
    private static final Map<String, List<InetAddress>> CACHE_V6 = new ConcurrentHashMap<>();
    
    // DNS resolver configuration constants
    private static final int DNS_TIMEOUT_SECONDS = 10;
    private static final int DNS_RETRIES = 3;

    public static Resolver resolver;

    static {
        Resolver r = null;
        try {
            ExtendedResolver extendedResolver = new ExtendedResolver(); // Uses system-configured resolvers
            // Configure timeouts and retries to avoid long DNS delays
            extendedResolver.setTimeout(Duration.ofSeconds(DNS_TIMEOUT_SECONDS));
            extendedResolver.setRetries(DNS_RETRIES);
            r = extendedResolver;
            logger.info("DNS Resolver initialized");
            logger.debug("sDNS settings: {} seconds timeout and {} retries.",  DNS_TIMEOUT_SECONDS, DNS_RETRIES);
        } catch (Exception e) {
            logger.error("Failed to initialize DNS resolver.", e);
        }
        resolver = r;
    }

    private DNSCacheResolver() {
    }

    /**
     * Initializes the DNS resolver with an optional custom DNS server.
     *
     * @param customDnsServer the custom DNS server to use, or null to use system default
     * @throws RuntimeException if the DNS resolver configuration is invalid
     */
    public static synchronized void initializeResolver(String customDnsServer) {
        try {
            if (customDnsServer != null && !customDnsServer.isEmpty()) {
                SimpleResolver simpleResolver = new SimpleResolver(customDnsServer);
                simpleResolver.setTimeout(Duration.ofSeconds(DNS_TIMEOUT_SECONDS));
                resolver = simpleResolver;
                logger.debug("DNS Resolver configured with custom server: {} ({}s timeout)",
                           customDnsServer, DNS_TIMEOUT_SECONDS);
            } else {
                ExtendedResolver extendedResolver = new ExtendedResolver(); // Uses system-configured resolvers
                extendedResolver.setTimeout(Duration.ofSeconds(DNS_TIMEOUT_SECONDS));
                extendedResolver.setRetries(DNS_RETRIES);
                resolver = extendedResolver;
                logger.debug("DNS Resolver initialized using system DNS settings with {}s timeout and {} retries.",
                           DNS_TIMEOUT_SECONDS, DNS_RETRIES);
            }
        } catch (Exception e) {
            logger.error("Failed to initialize DNS resolver with server: " + customDnsServer, e);
            throw new RuntimeException("Invalid DNS resolver configuration", e);
        }
    }

    /**
     * Initializes DNS resolution by performing lookups for the hostname in the given URL.
     *
     * <p>This method extracts the hostname from the URL and performs both A and AAAA
     * record lookups, caching the results for subsequent use. This is typically called
     * once at the beginning of validation to populate the DNS cache.</p>
     *
     * @param url the URL containing the hostname to resolve
     * @throws URISyntaxException if the URL is malformed
     */
    public static void initFromUrl(String url) {
        logger.debug("Trying to lookup FQDN for URL: {}", url);
        if (url == null) {
            logger.debug("URL is null, skipping DNS lookup");
            return;
        }
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                logger.debug("No host found in URL: {}", url);
                return;
            }
            String fqdn = ensureFQDN(host);
            resolveIfNeeded(fqdn);
        } catch (URISyntaxException e) {
            logger.error("Invalid URL: {}", url, e);
        }
    }

    /**
     * Retrieves the first IPv4 address for the given fully qualified domain name.
     *
     * @param fqdn the fully qualified domain name to resolve
     * @return the first IPv4 address found, or null if none available
     */
    public static InetAddress getFirstV4Address(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return getFirst(CACHE_V4, name);
    }

    /**
     * Retrieves the first IPv6 address for the given fully qualified domain name.
     *
     * @param fqdn the fully qualified domain name to resolve
     * @return the first IPv6 address found, or null if none available
     */
    public static InetAddress getFirstV6Address(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return getFirst(CACHE_V6, name);
    }

    public static List<InetAddress> getAllV4Addresses(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return Collections.unmodifiableList(CACHE_V4.getOrDefault(name, Collections.emptyList()));
    }

    /**
     * Checks if the hostname in the given URI has any IPv4 addresses.
     *
     * @param uri the URI containing the hostname to check
     * @return true if IPv4 addresses are available, false otherwise
     */
    public static boolean hasV4Addresses(String uri) {
        String fqdn = getHostnameFromUrl(uri);
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        List<InetAddress> addresses = CACHE_V4.getOrDefault(name, Collections.emptyList());
        return !addresses.isEmpty();
    }

    /**
     * Checks if the hostname in the given URI has any IPv6 addresses.
     *
     * @param uri the URI containing the hostname to check
     * @return true if IPv6 addresses are available, false otherwise
     */
    public static boolean hasV6Addresses(String uri) {
        String fqdn = getHostnameFromUrl(uri);
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        List<InetAddress> addresses = CACHE_V6.getOrDefault(name, Collections.emptyList());
        return !addresses.isEmpty();
    }

    public static List<InetAddress> getAllV6Addresses(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return Collections.unmodifiableList(CACHE_V6.getOrDefault(name, Collections.emptyList()));
    }

    public static boolean hasNoAddresses(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);

        List<InetAddress> v4Addresses = CACHE_V4.getOrDefault(name, Collections.emptyList());
        List<InetAddress> v6Addresses = CACHE_V6.getOrDefault(name, Collections.emptyList());

        return v4Addresses.isEmpty() && v6Addresses.isEmpty();
    }

    public static void resolveIfNeeded(String fqdn) {
        if (CACHE_V4.containsKey(fqdn) && CACHE_V6.containsKey(fqdn)) {
            logger.debug("Cache hit for {}", fqdn);
            return;
        }

        if (fqdn.equals(LOCAL_IPv4 + DOT) || fqdn.equals(LOCALHOST + DOT)) {
            logger.debug("Handling special-case loopback (IPv4) for {}", fqdn);
            try {
                CACHE_V4.put(fqdn, List.of(InetAddress.getByName(LOCAL_IPv4)));
            } catch (Exception e) {
                logger.debug("Failed to handle 127.0.0.1", e);
                CACHE_V4.put(fqdn, List.of());
            }
        } else {
            CACHE_V4.put(fqdn, resolveWithCNAMEChain(fqdn, Type.A));
        }

        if (fqdn.equals(LOCAL_IPv6 + DOT) || fqdn.equals(LOCALHOST + DOT)) {
            logger.debug("Handling special-case loopback (IPv6) for {}", fqdn);
            try {
                CACHE_V6.put(fqdn, List.of(InetAddress.getByName(LOCAL_IPv6)));
            } catch (Exception e) {
                logger.debug("Failed to handle ::1", e);
                CACHE_V6.put(fqdn, List.of());
            }
        } else {
            CACHE_V6.put(fqdn, resolveWithCNAMEChain(fqdn, Type.AAAA));
        }
    }

    public static List<InetAddress> resolveWithCNAMEChain(String fqdn, int type) {
        List<InetAddress> results = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String currentName = fqdn;

        while (true) {
            if (!visited.add(currentName)) {
                logger.debug("Detected CNAME loop involving: {}", currentName);
                break;
            }

            try {
                Name name = Name.fromString(currentName, Name.root);
                Record question = Record.newRecord(name, type, DClass.IN);
                Message query = Message.newQuery(question);
                Message response = resolver.send(query);

                boolean foundCname = false;

                for (Record answer : response.getSection(Section.ANSWER)) {
                    if (type == Type.A && answer instanceof ARecord) {
                        results.add(((ARecord) answer).getAddress());
                    } else if (type == Type.AAAA && answer instanceof AAAARecord) {
                        results.add(((AAAARecord) answer).getAddress());
                    } else if (answer instanceof CNAMERecord) {
                        CNAMERecord cname = (CNAMERecord) answer;
                        currentName = cname.getTarget().toString();
                        foundCname = true;
                        logger.debug("Following CNAME: {} → {}", cname.getName(), currentName);
                        break; // loop with new name
                    }
                }

                if (!foundCname) {
                    break;
                }

            } catch (Exception e) {
                logger.error("Error resolving {} [{}]", currentName, Type.string(type), e);
                break;
            }
        }

        logger.debug("Final resolved {} [{}] → {} record(s)", fqdn, Type.string(type), results.size());
        return results;
    }

    /**
     * Extracts the hostname from a URL string.
     *
     * @param url the URL to parse
     * @return the hostname portion of the URL, or empty string if parsing fails
     */
    public static String getHostnameFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String hostname = uri.getHost();
            return hostname != null ? hostname : "";
        } catch (URISyntaxException e) {
            logger.error("Failed to parse URL: {}", url, e);
            return "";
        }
    }

    public static InetAddress getFirst(Map<String, List<InetAddress>> cache, String fqdn) {
        List<InetAddress> list = cache.get(fqdn);
        return (list != null && !list.isEmpty()) ? list.getFirst() : null;
    }

    public static String ensureFQDN(String host) {
        return host.endsWith(DOT) ? host : host + DOT;
    }

    public static void doZeroIPAddressesValidation(String url, boolean executeIPv6Queries, boolean executeIPv4Queries) {
        String hostname = getHostnameFromUrl(url);
        if (hostname.isEmpty()) {
            addErrorToResultsFile(-13019, "no response available",
                "Unable to resolve an IP address endpoint using DNS.");
            return;
        }

        RDAPValidatorResults results = RDAPValidatorResultsImpl.getInstance();
        boolean hasV4 = hasV4Addresses(url);
        boolean hasV6 = hasV6Addresses(url);

        if (executeIPv4Queries && executeIPv6Queries && !hasV4 && !hasV6) {
            results.add(RDAPValidationResult.builder()
                                            .acceptHeader(DASH)
                                            .queriedURI(DASH)
                                            .httpMethod(DASH)
                                            .httpStatusCode(ZERO)
                                            .code(-13019)
                                            .value("no response available")
                                            .message("Unable to resolve an IP address endpoint using DNS.")
                                            .build());
            return;
        }

        // Because there is the possibility of the TIG1_8 not being run b/c we are running just v4 flags and no 2019 and no 2024 and/or the validator cuts out early, we _must_ check for -20400 as well
        if (executeIPv4Queries && !hasV4) {
            results.add(RDAPValidationResult.builder()
                                            .acceptHeader(DASH)
                                            .queriedURI(DASH)
                                            .httpMethod(DASH)
                                            .httpStatusCode(ZERO)
                                            .code(-20400)
                                            .value(hostname)
                                            .message(
                                                "The RDAP service is not provided over IPv4 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1.")
                                            .build());
        }

        if (executeIPv6Queries && !hasV6) {
            results.add(RDAPValidationResult.builder()
                                            .acceptHeader(DASH)
                                            .queriedURI(DASH)
                                            .httpMethod(DASH)
                                            .httpStatusCode(ZERO)
                                            .code(-20401)
                                            .value(hostname)
                                            .message(
                                                "The RDAP service is not provided over IPv6 or contains invalid addresses. See section 1.8 of the RDAP_Technical_Implementation_Guide_2_1.")
                                            .build());
        }
    }
}
