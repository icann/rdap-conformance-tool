package org.icann.rdapconformance.validator.util;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

public class IPv6ValidationUtilTest {

  @Test
  public void testValidIPv6Syntax() {
    // Test valid IPv6 addresses
    assertTrue(IPv6ValidationUtil.isValidIPv6Syntax("ffff:41d0:404:200::2df6"),
        "IPv6 address from bug report should have valid syntax");
    assertTrue(IPv6ValidationUtil.isValidIPv6Syntax("2001:db8::1"),
        "Standard IPv6 address should have valid syntax");
    assertTrue(IPv6ValidationUtil.isValidIPv6Syntax("::1"),
        "Loopback IPv6 address should have valid syntax");
    assertTrue(IPv6ValidationUtil.isValidIPv6Syntax("2001:4860:4860::8888"),
        "Google DNS IPv6 should have valid syntax");
  }

  @Test
  public void testInvalidIPv6Syntax() {
    // Test invalid IPv6 addresses
    assertFalse(IPv6ValidationUtil.isValidIPv6Syntax("invalid"),
        "Completely invalid string should fail");
    assertFalse(IPv6ValidationUtil.isValidIPv6Syntax("192.168.1.1"),
        "IPv4 address should fail IPv6 validation");
    assertFalse(IPv6ValidationUtil.isValidIPv6Syntax(""),
        "Empty string should fail");
    assertFalse(IPv6ValidationUtil.isValidIPv6Syntax(null),
        "Null should fail");
    assertFalse(IPv6ValidationUtil.isValidIPv6Syntax("gggg::1"),
        "Invalid hex characters should fail");
  }

  @Test
  public void testSpecificBugAddress() {
    // The specific address from the bug report should be recognized as valid syntax
    // but will fail semantic validation (not global unicast)
    String bugAddress = "ffff:41d0:404:200::2df6";
    assertTrue(IPv6ValidationUtil.isValidIPv6Syntax(bugAddress),
        "Bug report IPv6 address should be valid syntax (the issue is semantic, not syntactic)");
  }
}