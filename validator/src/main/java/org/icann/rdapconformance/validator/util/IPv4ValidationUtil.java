package org.icann.rdapconformance.validator.util;

import inet.ipaddr.IPAddressString;
import org.icann.rdapconformance.validator.CommonUtils;

/**
 * Utility class for IPv4 address validation.
 *
 * Provides shared validation logic for IPv4 addresses used across multiple
 * exception parsers to avoid code duplication and ensure consistent validation.
 */
public final class IPv4ValidationUtil {

  // Private constructor to prevent instantiation
  private IPv4ValidationUtil() {
    throw new UnsupportedOperationException("Utility class should not be instantiated");
  }

  /**
   * Validates IPv4 syntax using strict dot-decimal notation requirements.
   *
   * This method performs two-stage validation:
   * 1. Checks for basic dot-decimal pattern (4 numeric segments separated by dots)
   * 2. Uses IPAddressString library for comprehensive IPv4 validation
   *
   * @param ip the IP address string to validate
   * @return true if the IP address has valid IPv4 dot-decimal syntax, false otherwise
   */
  public static boolean isValidIPv4Syntax(String ip) {
    if (ip == null || ip.trim().isEmpty()) {
      return false;
    }

    try {
      // First check if it matches the basic dot-decimal pattern (4 segments separated by dots)
      if (!ip.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
        return false;
      }

      // Use IPAddressString to validate the actual IP address
      IPAddressString ipAddressString = new IPAddressString(ip);
      return ipAddressString.toAddress().isIPv4();
    } catch (Exception e) {
      // Any exception means invalid syntax
      return false;
    }
  }

  /**
   * Standard IPv4 pattern used in JSON schema validation.
   * This pattern is used to identify IPv4 pattern validation errors.
   */
  public static final String IPV4_DOT_DECIMAL_PATTERN = CommonUtils.IPV4_DOT_DECIMAL_PATTERN;
}