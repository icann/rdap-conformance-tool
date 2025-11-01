package org.icann.rdapconformance.validator;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.DOT;
import static org.icann.rdapconformance.validator.CommonUtils.LOCALHOST;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv4;
import static org.icann.rdapconformance.validator.CommonUtils.LOCAL_IPv6;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

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

    // Instance-based caches for thread safety
    private final Map<String, List<InetAddress>> cacheV4 = new ConcurrentHashMap<>();
    private final Map<String, List<InetAddress>> cacheV6 = new ConcurrentHashMap<>();
    
    // DNS resolver configuration constants
    private static final int DNS_TIMEOUT_SECONDS = 10;
    private static final int DNS_RETRIES = 3;

    // Instance-based resolver for thread safety
    private final Resolver resolver;

    static {
        logger.info("DNS Resolver initialized");
        logger.debug("DNS settings: {} seconds timeout and {} retries.", DNS_TIMEOUT_SECONDS, DNS_RETRIES);
    }

    // Public constructor for QueryContext usage
    public DNSCacheResolver() {
        this(null);
    }

    // Constructor with custom DNS server for testing
    public DNSCacheResolver(String customDnsServer) {
        this.resolver = initializeResolver(customDnsServer);
    }

    /**
     * Tests if the custom DNS resolver is reachable and responding.
     * Performs a quick health check query for icann.org.
     *
     * @param resolver the resolver to test
     * @return true if resolver responds within timeout, false otherwise
     */
    private static boolean checkResolverReachability(Resolver resolver) {
        try {
            // Test with icann.org - a well-known domain maintained by ICANN
            Name testName = Name.fromString(CommonUtils.ICANN_ORG_FQDN);
            Record question = Record.newRecord(testName, Type.A, DClass.IN);
            Message query = Message.newQuery(question);

            // Set a short timeout just for the health check (2 seconds)
            Duration originalTimeout = null;
            if (resolver instanceof SimpleResolver) {
                SimpleResolver sr = (SimpleResolver) resolver;
                originalTimeout = sr.getTimeout();
                sr.setTimeout(Duration.ofSeconds(2));
            }

            try {
                Message response = resolver.send(query);
                // If we got any response (even NXDOMAIN), the server is reachable
                return response != null;
            } finally {
                // Restore original timeout
                if (originalTimeout != null && resolver instanceof SimpleResolver) {
                    ((SimpleResolver) resolver).setTimeout(originalTimeout);
                }
            }
        } catch (Exception e) {
            logger.debug("DNS resolver health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Initializes the DNS resolver with an optional custom DNS server.
     *
     * @param customDnsServer the custom DNS server to use, or null to use system default
     * @return the configured Resolver instance
     * @throws RuntimeException if the DNS resolver configuration is invalid
     */
    private Resolver initializeResolver(String customDnsServer) {
        try {
            if (customDnsServer != null && !customDnsServer.isEmpty()) {
                SimpleResolver simpleResolver = new SimpleResolver(customDnsServer);
                simpleResolver.setTimeout(Duration.ofSeconds(DNS_TIMEOUT_SECONDS));

                // Skip health check for localhost IPs (used for testing)
                boolean isLocalhost = customDnsServer.equals(CommonUtils.LOCAL_IPv4) ||
                                     customDnsServer.equals(CommonUtils.LOCAL_IPv6_COMPRESSED) ||
                                     customDnsServer.equals(CommonUtils.LOCALHOST);

                // IMPORTANT: Test if the resolver is actually reachable (unless it's localhost)
                if (!isLocalhost && !checkResolverReachability(simpleResolver)) {
                    throw new RuntimeException(
                        "DNS server " + customDnsServer + " is not responding. " +
                        "Please verify the IP address is correct and the server is reachable.");
                }

                logger.debug("DNS Resolver configured with custom server: {} ({}s timeout)",
                           customDnsServer, DNS_TIMEOUT_SECONDS);
                return simpleResolver;
            } else {
                ExtendedResolver extendedResolver = new ExtendedResolver(); // Uses system-configured resolvers
                extendedResolver.setTimeout(Duration.ofSeconds(DNS_TIMEOUT_SECONDS));
                extendedResolver.setRetries(DNS_RETRIES);
                logger.debug("DNS Resolver initialized using system DNS settings with {}s timeout and {} retries.",
                           DNS_TIMEOUT_SECONDS, DNS_RETRIES);
                return extendedResolver;
            }
        } catch (RuntimeException e) {
            // Re-throw RuntimeException (from health check failure) as-is
            throw e;
        } catch (Exception e) {
            logger.error("Failed to initialize DNS resolver with server '{}': {}",
                       customDnsServer, e.getMessage());
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
    public void initFromUrl(String url) {
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
    public InetAddress getFirstV4Address(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return getFirst(cacheV4, name);
    }

    /**
     * Retrieves the first IPv6 address for the given fully qualified domain name.
     *
     * @param fqdn the fully qualified domain name to resolve
     * @return the first IPv6 address found, or null if none available
     */
    public InetAddress getFirstV6Address(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return getFirst(cacheV6, name);
    }

    public List<InetAddress> getAllV4Addresses(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return Collections.unmodifiableList(cacheV4.getOrDefault(name, Collections.emptyList()));
    }

    /**
     * Checks if the hostname in the given URI has any IPv4 addresses.
     *
     * @param uri the URI containing the hostname to check
     * @return true if IPv4 addresses are available, false otherwise
     */
    public boolean hasV4Addresses(String uri) {
        String fqdn = getHostnameFromUrl(uri);
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        List<InetAddress> addresses = cacheV4.getOrDefault(name, Collections.emptyList());
        return !addresses.isEmpty();
    }

    /**
     * Checks if the hostname in the given URI has any IPv6 addresses.
     *
     * @param uri the URI containing the hostname to check
     * @return true if IPv6 addresses are available, false otherwise
     */
    public boolean hasV6Addresses(String uri) {
        String fqdn = getHostnameFromUrl(uri);
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        List<InetAddress> addresses = cacheV6.getOrDefault(name, Collections.emptyList());
        return !addresses.isEmpty();
    }

    public List<InetAddress> getAllV6Addresses(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return Collections.unmodifiableList(cacheV6.getOrDefault(name, Collections.emptyList()));
    }

    public boolean hasNoAddresses(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);

        List<InetAddress> v4Addresses = cacheV4.getOrDefault(name, Collections.emptyList());
        List<InetAddress> v6Addresses = cacheV6.getOrDefault(name, Collections.emptyList());

        return v4Addresses.isEmpty() && v6Addresses.isEmpty();
    }

    private void resolveIfNeeded(String fqdn) {
        if (cacheV4.containsKey(fqdn) && cacheV6.containsKey(fqdn)) {
            logger.debug("Cache hit for {}", fqdn);
            return;
        }

        if (fqdn.equals(LOCAL_IPv4 + DOT) || fqdn.equals(LOCALHOST + DOT)) {
            logger.debug("Handling special-case loopback (IPv4) for {}", fqdn);
            try {
                cacheV4.put(fqdn, List.of(InetAddress.getByName(CommonUtils.LOCAL_IPv4)));
            } catch (Exception e) {
                logger.debug("Failed to handle {}", CommonUtils.LOCAL_IPv4, e);
                cacheV4.put(fqdn, List.of());
            }
        } else {
            cacheV4.put(fqdn, resolveWithCNAMEChain(fqdn, Type.A));
        }

        if (fqdn.equals(LOCAL_IPv6 + DOT) || fqdn.equals(LOCALHOST + DOT)) {
            logger.debug("Handling special-case loopback (IPv6) for {}", fqdn);
            try {
                cacheV6.put(fqdn, List.of(InetAddress.getByName(CommonUtils.LOCAL_IPv6_COMPRESSED)));
            } catch (Exception e) {
                logger.debug("Failed to handle {}", CommonUtils.LOCAL_IPv6_COMPRESSED, e);
                cacheV6.put(fqdn, List.of());
            }
        } else {
            cacheV6.put(fqdn, resolveWithCNAMEChain(fqdn, Type.AAAA));
        }
    }

    private List<InetAddress> resolveWithCNAMEChain(String fqdn, int type) {
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

    public static void doZeroIPAddressesValidation(QueryContext queryContext, String url, boolean executeIPv6Queries, boolean executeIPv4Queries) {
        String hostname = getHostnameFromUrl(url);
        if (hostname.isEmpty()) {
            queryContext.addError(-13019, "no response available",
                "Unable to resolve an IP address endpoint using DNS.");
            return;
        }

        RDAPValidatorResults results = queryContext.getResults();
        DNSCacheResolver dnsResolver = queryContext.getDnsResolver();
        boolean hasV4 = dnsResolver.hasV4Addresses(url);
        boolean hasV6 = dnsResolver.hasV6Addresses(url);

        if (executeIPv4Queries && executeIPv6Queries && !hasV4 && !hasV6) {
            results.add(RDAPValidationResult.builder()
                                            .acceptHeader(DASH)
                                            .queriedURI(DASH)
                                            .httpMethod(DASH)
                                            .httpStatusCode(ZERO)
                                            .code(-13019)
                                            .value("no response available")
                                            .message("Unable to resolve an IP address endpoint using DNS.")
                                            .build(queryContext));
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
                                            .build(queryContext));
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
                                            .build(queryContext));
        }
    }
}
