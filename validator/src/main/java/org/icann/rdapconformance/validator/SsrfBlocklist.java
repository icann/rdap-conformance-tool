package org.icann.rdapconformance.validator;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Decides whether an IP address is an acceptable destination for an outbound HTTP connection.
 *
 * <p>Two independent rules are combined:</p>
 * <ul>
 *   <li><strong>Built-in private/reserved ranges</strong> (always on): loopback, RFC 1918,
 *       link-local, wildcard, IPv6 ULA {@code fc00::/7} and the cloud metadata address
 *       {@code 169.254.169.254}.</li>
 *   <li><strong>Operator-configured CIDR ranges</strong>: globally routable prefixes that must
 *       nevertheless be treated as internal (for example an organisation's own public address
 *       space, whose services may trust the tool's egress IP via IP ACLs). Empty by default.</li>
 * </ul>
 *
 * <p>Instances are immutable and safe to share between threads.</p>
 *
 * @see org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration#getSsrfBlockedCidrs()
 */
public final class SsrfBlocklist {

    /** Cloud provider instance-metadata endpoint (AWS, GCP, Azure, ...). */
    public static final String CLOUD_METADATA_IP = "169.254.169.254";

    /** Environment variable naming a CIDR file to load in addition to the configured list. */
    public static final String CIDR_FILE_ENV = "RDAPCT_SSRF_BLOCKED_CIDRS_FILE";

    private static final SsrfBlocklist EMPTY = new SsrfBlocklist(Collections.emptyList());

    private final List<IPAddress> cidrs;

    private SsrfBlocklist(List<IPAddress> cidrs) {
        this.cidrs = List.copyOf(cidrs);
    }

    /** @return a blocklist with no configured CIDRs (built-in private ranges still apply). */
    public static SsrfBlocklist empty() {
        return EMPTY;
    }

    /**
     * Builds a blocklist from CIDR strings. Bare IP literals are accepted and treated as
     * {@code /32} or {@code /128}. Blank entries and {@code #} comments are ignored.
     *
     * @throws IllegalArgumentException if any entry is not a valid IPv4/IPv6 address or prefix
     */
    public static SsrfBlocklist of(Collection<String> cidrStrings) {
        if (cidrStrings == null || cidrStrings.isEmpty()) {
            return EMPTY;
        }
        List<IPAddress> parsed = new ArrayList<>();
        for (String raw : cidrStrings) {
            String entry = stripComment(raw);
            if (entry.isEmpty()) {
                continue;
            }
            parsed.add(parseCidr(entry));
        }
        return parsed.isEmpty() ? EMPTY : new SsrfBlocklist(parsed);
    }

    /**
     * Reads a CIDR file: one entry per line, blank lines and {@code #} comments ignored.
     *
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if any line is not a valid IPv4/IPv6 address or prefix
     */
    public static List<String> readCidrFile(Path file) throws IOException {
        List<String> entries = new ArrayList<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNo++;
            String entry = stripComment(line);
            if (entry.isEmpty()) {
                continue;
            }
            try {
                parseCidr(entry);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(file + ":" + lineNo + ": " + e.getMessage(), e);
            }
            entries.add(entry);
        }
        return entries;
    }

    /** @return number of configured CIDR ranges (built-in ranges are not counted). */
    public int size() {
        return cidrs.size();
    }

    /** @return true if an address is blocked by the built-in ranges or a configured CIDR. */
    public boolean isBlocked(InetAddress addr) {
        return isPrivateOrReserved(addr) || isInConfiguredRange(addr);
    }

    /** @return true if an address falls inside any operator-configured CIDR. */
    public boolean isInConfiguredRange(InetAddress addr) {
        if (addr == null || cidrs.isEmpty()) {
            return false;
        }
        IPAddress candidate = new IPAddressString(addr.getHostAddress()).getAddress();
        if (candidate == null) {
            return false;
        }
        // Compare IPv4-mapped IPv6 (::ffff:a.b.c.d) against IPv4 prefixes as well
        IPAddress v4View = candidate.isIPv6() && candidate.isIPv4Convertible() ? candidate.toIPv4() : null;
        for (IPAddress cidr : cidrs) {
            if (cidr.contains(candidate) || (v4View != null && cidr.contains(v4View))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return true for loopback, site-local (RFC 1918 / fec0::/10), link-local, wildcard,
     *         IPv6 ULA (fc00::/7) and the cloud metadata address
     */
    public static boolean isPrivateOrReserved(InetAddress addr) {
        if (addr == null) {
            return false;
        }
        return addr.isLoopbackAddress()
                || addr.isSiteLocalAddress()
                || addr.isLinkLocalAddress()
                || addr.isAnyLocalAddress()
                || isIPv6UniqueLocalAddress(addr)
                || CLOUD_METADATA_IP.equals(addr.getHostAddress());
    }

    /**
     * {@link InetAddress#isSiteLocalAddress()} only covers the deprecated {@code fec0::/10}
     * (RFC 3879); RFC 4193 Unique Local Addresses use {@code fc00::/7}.
     */
    public static boolean isIPv6UniqueLocalAddress(InetAddress addr) {
        if (addr instanceof Inet6Address) {
            // fc00::/7 — first 7 bits are 1111110x, so byte0 & 0xFE == 0xFC
            return (addr.getAddress()[0] & 0xFE) == 0xFC;
        }
        return false;
    }

    private static IPAddress parseCidr(String entry) {
        IPAddress addr = new IPAddressString(entry).getAddress();
        if (addr == null) {
            throw new IllegalArgumentException("Invalid IP address or CIDR: '" + entry + "'");
        }
        // Java hands IPv4-mapped destinations (::ffff:a.b.c.d) to us as Inet4Address, so a
        // mapped range in the configuration must be matched as its IPv4 equivalent.
        if (addr.isIPv6() && addr.isIPv4Convertible()) {
            addr = addr.toIPv4();
        }
        // Normalise to the network prefix so "10.1.2.3/8" matches the whole 10/8 block
        return addr.isPrefixed() ? addr.toPrefixBlock() : addr;
    }

    private static String stripComment(String line) {
        if (line == null) {
            return "";
        }
        int hash = line.indexOf('#');
        return (hash >= 0 ? line.substring(0, hash) : line).trim();
    }

    @Override
    public String toString() {
        return "SsrfBlocklist" + cidrs;
    }
}
