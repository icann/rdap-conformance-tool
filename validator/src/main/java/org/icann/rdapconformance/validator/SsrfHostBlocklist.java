package org.icann.rdapconformance.validator;

import inet.ipaddr.IPAddressString;
import java.io.IOException;
import java.net.IDN;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Hostname-based SSRF denylist, the DNS-name counterpart of {@link SsrfBlocklist}.
 *
 * <p>Entries are either an exact hostname ({@code icann.org}) or a leading wildcard
 * ({@code *.icann.org}). Matching rules:</p>
 * <ul>
 *   <li>Case-insensitive; a trailing dot is ignored; IDN labels are compared in A-label
 *       (punycode) form via {@link IDN#toASCII(String)}.</li>
 *   <li>{@code *.example} matches every subdomain at any depth ({@code a.example},
 *       {@code a.b.example}) but <strong>not</strong> the apex {@code example} itself — list the
 *       apex as a separate exact entry when it must be blocked too.</li>
 *   <li>Comparison is on label boundaries: {@code *.icann.org} matches neither
 *       {@code evilicann.org} nor {@code icann.org.evil.com}.</li>
 *   <li>IP literals never match a hostname entry; address policy lives in {@link SsrfBlocklist}.</li>
 * </ul>
 *
 * <p>Instances are immutable and safe to share between threads.</p>
 *
 * @see org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration#getSsrfBlockedHosts()
 */
public final class SsrfHostBlocklist {

    /** Environment variable naming a hostname file to load in addition to the configured list. */
    public static final String HOSTS_FILE_ENV = "RDAPCT_SSRF_BLOCKED_HOSTS_FILE";

    private static final String WILDCARD_PREFIX = "*.";
    private static final int MAX_HOSTNAME_LENGTH = 253;
    /** RFC 1123 label: 1-63 alphanumerics or hyphens, not starting/ending with a hyphen. */
    private static final Pattern LABEL = Pattern.compile("[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?");

    private static final SsrfHostBlocklist EMPTY =
            new SsrfHostBlocklist(Collections.emptySet(), Collections.emptySet());

    private final Set<String> exact;
    /** Wildcard suffixes stored without the {@code *} — e.g. {@code .icann.org}. */
    private final Set<String> wildcardSuffixes;

    private SsrfHostBlocklist(Set<String> exact, Set<String> wildcardSuffixes) {
        this.exact = Set.copyOf(exact);
        this.wildcardSuffixes = Set.copyOf(wildcardSuffixes);
    }

    /** @return a blocklist with no hostname entries. */
    public static SsrfHostBlocklist empty() {
        return EMPTY;
    }

    /**
     * Builds a blocklist from hostname / wildcard strings. Blank entries and {@code #} comments
     * are ignored.
     *
     * @throws IllegalArgumentException if any entry is not a valid hostname or {@code *.hostname}
     */
    public static SsrfHostBlocklist of(Collection<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return EMPTY;
        }
        Set<String> exact = new LinkedHashSet<>();
        Set<String> wildcards = new LinkedHashSet<>();
        for (String raw : entries) {
            String entry = stripComment(raw);
            if (entry.isEmpty()) {
                continue;
            }
            if (entry.startsWith(WILDCARD_PREFIX)) {
                wildcards.add("." + normalizeHostname(entry.substring(WILDCARD_PREFIX.length()), entry));
            } else {
                exact.add(normalizeHostname(entry, entry));
            }
        }
        return exact.isEmpty() && wildcards.isEmpty() ? EMPTY : new SsrfHostBlocklist(exact, wildcards);
    }

    /**
     * Reads a hostname file: one entry per line, blank lines and {@code #} comments ignored.
     *
     * @throws IOException if the file cannot be read
     * @throws IllegalArgumentException if any line is not a valid hostname or {@code *.hostname}
     */
    public static List<String> readHostFile(Path file) throws IOException {
        List<String> entries = new ArrayList<>();
        int lineNo = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNo++;
            String entry = stripComment(line);
            if (entry.isEmpty()) {
                continue;
            }
            try {
                of(List.of(entry));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(file + ":" + lineNo + ": " + e.getMessage(), e);
            }
            entries.add(entry);
        }
        return entries;
    }

    /** @return number of configured entries (exact + wildcard). */
    public int size() {
        return exact.size() + wildcardSuffixes.size();
    }

    /**
     * @return true if {@code host} is an exact entry or falls under a wildcard entry. IP literals,
     *         {@code null} and unparsable names never match.
     */
    public boolean isBlocked(String host) {
        if (host == null || size() == 0) {
            return false;
        }
        String candidate = host.trim();
        if (candidate.isEmpty() || isIpLiteral(candidate)) {
            return false;
        }
        String normalized;
        try {
            normalized = canonical(candidate);
        } catch (IllegalArgumentException e) {
            return false;
        }
        if (exact.contains(normalized)) {
            return true;
        }
        for (String suffix : wildcardSuffixes) {
            // suffix carries its leading dot, so this is a label-boundary match
            if (normalized.length() > suffix.length() && normalized.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    /** Validates and canonicalises a configuration entry; {@code original} is only for messages. */
    private static String normalizeHostname(String host, String original) {
        if (host.isEmpty() || host.indexOf('*') >= 0) {
            throw new IllegalArgumentException(
                    "Invalid hostname entry '" + original + "': only a leading '*.' wildcard is supported");
        }
        if (isIpLiteral(host)) {
            throw new IllegalArgumentException(
                    "Invalid hostname entry '" + original + "': IP addresses belong in the CIDR blocklist");
        }
        String normalized;
        try {
            normalized = canonical(host);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid hostname entry '" + original + "': " + e.getMessage(), e);
        }
        if (normalized.length() > MAX_HOSTNAME_LENGTH) {
            throw new IllegalArgumentException("Invalid hostname entry '" + original + "': name too long");
        }
        for (String label : normalized.split("\\.", -1)) {
            if (!LABEL.matcher(label).matches()) {
                throw new IllegalArgumentException(
                        "Invalid hostname entry '" + original + "': invalid DNS label '" + label + "'");
            }
        }
        return normalized;
    }

    /** Lowercase A-label form without trailing dot. Throws IllegalArgumentException on bad IDN. */
    private static String canonical(String host) {
        String h = host;
        if (h.endsWith(".")) {
            h = h.substring(0, h.length() - 1);
        }
        return IDN.toASCII(h, IDN.ALLOW_UNASSIGNED).toLowerCase();
    }

    private static boolean isIpLiteral(String host) {
        String h = host;
        if (h.startsWith("[") && h.endsWith("]")) {
            h = h.substring(1, h.length() - 1);
        }
        return new IPAddressString(h).isIPAddress();
    }

    private static String stripComment(String line) {
        if (line == null) {
            return "";
        }
        int hash = line.indexOf('#');
        return (hash >= 0 ? line.substring(0, hash) : line).trim();
    }
}
