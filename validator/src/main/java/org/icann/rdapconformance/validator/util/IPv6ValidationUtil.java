package org.icann.rdapconformance.validator.util;

import inet.ipaddr.IPAddressString;

/**
 * Utility class for IPv6 address validation.
 *
 * Provides shared validation logic for IPv6 addresses used across multiple
 * exception parsers to avoid code duplication and ensure consistent validation.
 */
public final class IPv6ValidationUtil {

  // Private constructor to prevent instantiation
  private IPv6ValidationUtil() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  /**
   * Validates IPv6 syntax using standard IPv6 notation requirements.
   *
   * This method uses the IPAddressString library to determine if an IPv6 address
   * has valid syntax according to RFC 4291 and related standards. This distinguishes
   * between syntax errors (malformed IPv6) and semantic errors (valid syntax but
   * allocation/special address issues).
   *
   * @param ip the IP address string to validate
   * @return true if the IP address has valid IPv6 syntax, false otherwise
   */
  public static boolean isValidIPv6Syntax(String ip) {
    if (ip == null || ip.trim().isEmpty()) {
      return false;
    }

    try {
      // Use IPAddressString to validate the actual IPv6 address syntax
      IPAddressString ipAddressString = new IPAddressString(ip);
      return ipAddressString.toAddress().isIPv6();
    } catch (Exception e) {
      // Any exception means invalid syntax
      return false;
    }
  }
}