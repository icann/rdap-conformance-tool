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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.xbill.DNS.Record;

public class DNSCacheResolver {

    private static final Logger logger = LoggerFactory.getLogger(DNSCacheResolver.class);

    private static final Map<String, List<InetAddress>> CACHE_V4 = new ConcurrentHashMap<>();
    private static final Map<String, List<InetAddress>> CACHE_V6 = new ConcurrentHashMap<>();

    public static final Resolver resolver;

    static {
        Resolver r = null;
        try {
            r = new ExtendedResolver(); // Uses system-configured resolvers
            logger.info("DNS Resolver initialized using system DNS settings.");
        } catch (Exception e) {
            logger.error("Failed to initialize DNS resolver.", e);
        }
        resolver = r;
    }

    private DNSCacheResolver() {
    }

    public static void initFromUrl(String url) {
        logger.info("Trying to lookup FQDN for URL: {}", url);
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null || host.isEmpty()) {
                logger.info("No host found in URL: {}", url);
                return;
            }
            String fqdn = ensureFQDN(host);
            resolveIfNeeded(fqdn);
        } catch (URISyntaxException e) {
            logger.error("Invalid URL: {}", url, e);
        }
    }

    public static InetAddress getFirstV4Address(String fqdn) {
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        return getFirst(CACHE_V4, name);
    }

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

    public static boolean hasV4Addresses(String uri) {
        String fqdn = getHostnameFromUrl(uri);
        String name = ensureFQDN(fqdn);
        resolveIfNeeded(name);
        List<InetAddress> addresses = CACHE_V4.getOrDefault(name, Collections.emptyList());
        return !addresses.isEmpty();
    }

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
            logger.info("Cache hit for {}", fqdn);
            return;
        }

        if (fqdn.equals(LOCAL_IPv4 + DOT) || fqdn.equals(LOCALHOST + DOT)) {
            logger.info("Handling special-case loopback (IPv4) for {}", fqdn);
            try {
                CACHE_V4.put(fqdn, List.of(InetAddress.getByName(LOCAL_IPv4)));
            } catch (Exception e) {
                logger.info("Failed to handle 127.0.0.1", e);
                CACHE_V4.put(fqdn, List.of());
            }
        } else {
            CACHE_V4.put(fqdn, resolveWithCNAMEChain(fqdn, Type.A));
        }

        if (fqdn.equals(LOCAL_IPv6 + DOT) || fqdn.equals(LOCALHOST + DOT)) {
            logger.info("Handling special-case loopback (IPv6) for {}", fqdn);
            try {
                CACHE_V6.put(fqdn, List.of(InetAddress.getByName(LOCAL_IPv6)));
            } catch (Exception e) {
                logger.info("Failed to handle ::1", e);
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
                logger.info("Detected CNAME loop involving: {}", currentName);
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
                        logger.info("Following CNAME: {} → {}", cname.getName(), currentName);
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

        logger.info("Final resolved {} [{}] → {} record(s)", fqdn, Type.string(type), results.size());
        return results;
    }

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
