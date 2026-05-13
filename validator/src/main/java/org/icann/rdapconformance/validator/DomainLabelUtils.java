package org.icann.rdapconformance.validator;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared utilities for domain label type detection (A-label vs U-label).
 * Used by both IdnAwareUriConverter and RDAPHttpQueryTypeProcessor to ensure
 * consistent mixed-label detection across URI normalization and validation.
 */
public final class DomainLabelUtils {

    private DomainLabelUtils() {}

    /**
     * Returns true if the domain name contains both A-labels (xn--) and U-labels (non-ASCII
     * or percent-encoded non-ASCII bytes), which is invalid per RFC 5891.
     * Handles both decoded Unicode and percent-encoded forms (e.g. %E4%BE%8B).
     */
    public static boolean hasMixedLabels(String domainName) {
        if (domainName == null || domainName.isEmpty()) {
            return false;
        }

        // Try to decode percent-encoded chars first
        String decoded = domainName;
        try {
            decoded = URLDecoder.decode(domainName, "UTF-8");
        } catch (Exception e) {
            // decode failed — fall through to percent-encoded check below
        }

        boolean hasALabel = false;
        boolean hasULabel = false;

        for (String label : decoded.split("\\.")) {
            if (label.toLowerCase().startsWith("xn--")) {
                hasALabel = true;
            } else if (!isAscii(label)) {
                hasULabel = true;
            }
            if (hasALabel && hasULabel) return true;
        }

        // If decode failed (decoded == domainName), also check raw percent sequences
        if (decoded.equals(domainName)) {
            for (String label : domainName.split("\\.")) {
                if (label.toLowerCase().startsWith("xn--")) {
                    hasALabel = true;
                } else if (label.contains("%")) {
                    Matcher m =
                            Pattern.compile("%([0-9A-Fa-f]{2})").matcher(label);
                    while (m.find()) {
                        if (Integer.parseInt(m.group(1), 16) > 0x7F) {
                            hasULabel = true;
                            break;
                        }
                    }
                }
                if (hasALabel && hasULabel) return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the string contains only ASCII characters (codepoints 0–127).
     */
    public static boolean isAscii(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) > 127) return false;
        }
        return true;
    }
}