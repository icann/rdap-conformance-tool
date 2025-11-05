package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test to ensure IPv6 validation does not generate cascade errors.
 *
 * This test specifically addresses the issue where IPv6 addresses that are not
 * in global unicast space (error -10201) were incorrectly generating additional
 * cascade errors -12407 and -12208.
 *
 * See: RCT-435 - IPv6 false positive when non-allocated number
 */
public class SchemaValidatorIpv6CascadeErrorTest extends SchemaValidatorTest {

  public SchemaValidatorIpv6CascadeErrorTest() {
    super(
        "test_rdap_general_tests.json",
        "/validators/ipv6/valid.json");
    validationName = "IPv6Validation";
  }

  @BeforeMethod
  public void setUp() throws IOException {
    super.setUp();
    name = "ipv6";
  }

  /**
   * Test that IPv6 addresses not in global unicast space only generate -10201 error,
   * not cascade errors -12407 (stdRdapIpAddressesValidation) or -12208 (stdRdapNameserverLookupValidation).
   *
   * This is a regression test for RCT-435.
   */
  @Test
  public void testIpv6NonGlobalUnicastNoCascadeErrors() {
    // Mock the IPv6 address space dataset to return invalid for our test address
    doReturn(true).when(datasets.get(Ipv6AddressSpace.class)).isInvalid(any());

    // Set IPv6 address that's not in global unicast space
    jsonObject.put("ipv6", "ffff:41d0:404:200::2df6"); // Non-global unicast (starts with ffff:)

    // Validate the IPv6 address
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();

    // Get all results - convert Set to List
    List<RDAPValidationResult> allResults = results.getAll().stream().collect(Collectors.toList());

    // Get all error codes
    List<Integer> errorCodes = allResults.stream()
        .map(RDAPValidationResult::getCode)
        .collect(Collectors.toList());

    // Should contain -10201 (IPv6 not in global unicast)
    assertThat(errorCodes)
        .as("Should contain -10201 error for IPv6 not in global unicast space")
        .contains(-10201);

    // Should NOT contain cascade errors
    assertThat(errorCodes)
        .as("Should not contain cascade error -12407 (stdRdapIpAddressesValidation)")
        .doesNotContain(-12407);

    assertThat(errorCodes)
        .as("Should not contain cascade error -12208 (stdRdapNameserverLookupValidation)")
        .doesNotContain(-12208);

    // Verify the -10201 error has correct message and value
    List<RDAPValidationResult> ipv6Errors = allResults.stream()
        .filter(result -> result.getCode() == -10201)
        .collect(Collectors.toList());

    assertThat(ipv6Errors)
        .as("Should have exactly one -10201 error")
        .hasSize(1);

    RDAPValidationResult ipv6Error = ipv6Errors.get(0);
    assertThat(ipv6Error.getMessage())
        .contains("IPv6 address is not included in a prefix categorized as Global Unicast");

    assertThat(ipv6Error.getValue())
        .contains("ffff:41d0:404:200::2df6");
  }

  /**
   * Test that IPv6 addresses in special address space only generate -10202 error,
   * not cascade errors.
   */
  @Test
  public void testIpv6SpecialAddressNoCascadeErrors() {
    // Mock the special IPv6 addresses dataset to return invalid for our test address
    doReturn(true).when(datasets.get(SpecialIPv6Addresses.class)).isInvalid(any());

    // Set special IPv6 address
    jsonObject.put("ipv6", "::1"); // Loopback - special address

    // Validate the IPv6 address
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();

    // Get all results - convert Set to List
    List<RDAPValidationResult> allResults = results.getAll().stream().collect(Collectors.toList());

    // Get all error codes
    List<Integer> errorCodes = allResults.stream()
        .map(RDAPValidationResult::getCode)
        .collect(Collectors.toList());

    // Should contain -10202 (IPv6 in special address space)
    assertThat(errorCodes)
        .as("Should contain -10202 error for IPv6 in special address space")
        .contains(-10202);

    // Should NOT contain cascade errors
    assertThat(errorCodes)
        .as("Should not contain cascade error -12407 (stdRdapIpAddressesValidation)")
        .doesNotContain(-12407);

    assertThat(errorCodes)
        .as("Should not contain cascade error -12208 (stdRdapNameserverLookupValidation)")
        .doesNotContain(-12208);
  }

  /**
   * Test that syntactically invalid IPv6 addresses only generate -10200 error,
   * not cascade errors. (This verifies existing behavior remains correct)
   */
  @Test
  public void testIpv6SyntaxErrorNoCascadeErrors() {
    // Set syntactically invalid IPv6 address
    jsonObject.put("ipv6", "invalid-ipv6-syntax"); // Invalid syntax

    // Validate the IPv6 address
    assertThat(schemaValidator.validate(jsonObject.toString())).isFalse();

    // Get all results - convert Set to List
    List<RDAPValidationResult> allResults = results.getAll().stream().collect(Collectors.toList());

    // Get all error codes
    List<Integer> errorCodes = allResults.stream()
        .map(RDAPValidationResult::getCode)
        .collect(Collectors.toList());

    // Should contain -10200 (IPv6 syntax error)
    assertThat(errorCodes)
        .as("Should contain -10200 error for invalid IPv6 syntax")
        .contains(-10200);

    // Should NOT contain cascade errors (this was already working correctly)
    assertThat(errorCodes)
        .as("Should not contain cascade error -12407 (stdRdapIpAddressesValidation)")
        .doesNotContain(-12407);

    assertThat(errorCodes)
        .as("Should not contain cascade error -12208 (stdRdapNameserverLookupValidation)")
        .doesNotContain(-12208);
  }
}