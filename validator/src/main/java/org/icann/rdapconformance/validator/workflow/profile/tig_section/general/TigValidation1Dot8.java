package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import static org.icann.rdapconformance.validator.CommonUtils.DASH;
import static org.icann.rdapconformance.validator.CommonUtils.ZERO;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.icann.rdapconformance.validator.SchemaValidator;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.icann.rdapconformance.validator.DNSCacheResolver;

public final class TigValidation1Dot8 extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot8.class);
  static IPValidator ipValidator = new IPValidator();
  private final HttpResponse<String> rdapResponse;
  private final RDAPDatasetService datasetService;

  private final RDAPValidatorConfiguration config;

  public TigValidation1Dot8(HttpResponse<String> rdapResponse, RDAPValidatorResults results,
      RDAPDatasetService datasetService, RDAPValidatorConfiguration config) {
    super(results);
    this.rdapResponse = rdapResponse;
    this.datasetService = datasetService;
    this.config = config;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_8_Validation";
  }

  @Override
  public boolean doValidate() {
    if (rdapResponse == null) {
      logger.info("rdapResponse is null. Skipping validation.");
      return true; // Skip validation if rdapResponse is null
    }

    boolean isValid = true;
    Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
    while (responseOpt.isPresent()) {
      HttpResponse<String> response = responseOpt.get();
      if (!validateHost(response.uri(), results, datasetService, config)) {
        isValid = false;
      }
      responseOpt = response.previousResponse(); // there never should be a previous
    }
    return isValid;
  }

  public static boolean validateHost(URI uri, RDAPValidatorResults results,
                                     RDAPDatasetService datasetService, RDAPValidatorConfiguration config) {
    boolean isValid = true;
    String host = uri.getHost();
    if (host == null || host.isEmpty()) {
      logger.info("Error when retrieving RDAP server hostname in order to check "
          + "[tigSection_1_8_Validation]");
      return true;
    }

    // Use DNSCacheResolver for IPv4
    Set<InetAddress> ipv4Addresses = new HashSet<>(DNSCacheResolver.getAllV4Addresses(host));
    // dump out the addresses
    logger.debug("DNSDNSDNS---> IPv4 addresses for {}: {}", host, ipv4Addresses.stream()
        .map(InetAddress::getHostAddress)
        .collect(Collectors.toList()));

    // If we are validating over v4
    if(!config.isNoIpv4Queries()) {
      if (ipv4Addresses.isEmpty() || containsInvalidIPAddress(ipv4Addresses, datasetService)) {
        System.out.println("DNDDNSDNS something is wrong with the ipv4 addresses");
        results.add(RDAPValidationResult.builder()
                                        .acceptHeader(DASH)
                                        .queriedURI(DASH)
                                        .httpMethod(DASH)
                                        .httpStatusCode(ZERO)
                                        .code(-20400)
                                        .value(host)
                                        .message("The RDAP service is not provided over IPv4 or contains invalid addresses. See section 1.8 of the "
                                            + "RDAP_Technical_Implementation_Guide_2_1.")
                                        .build());
        isValid = false;
      }
    }

    // Use DNSCacheResolver for IPv6
    Set<InetAddress> ipv6Addresses = new HashSet<>(DNSCacheResolver.getAllV6Addresses(host));
    // If we are validating over v6
    if(!config.isNoIpv6Queries()) {
      if (ipv6Addresses.isEmpty() || containsInvalidIPAddress(ipv6Addresses, datasetService)) {
        results.add(RDAPValidationResult.builder()
                                        .acceptHeader(DASH)
                                        .queriedURI(DASH)
                                        .httpMethod(DASH)
                                        .httpStatusCode(ZERO)
                                        .code(-20401)
                                        .value(host)
                                        .message("The RDAP service is not provided over IPv6 or contains invalid addresses. See section 1.8 of the "
                                            + "RDAP_Technical_Implementation_Guide_2_1.")
                                        .build());
        isValid = false;
      }
    }

    return isValid;
  }

  private static boolean containsInvalidIPAddress(Set<InetAddress> addresses,
      RDAPDatasetService datasetService) {
    for (InetAddress address : addresses) {
      if (ipValidator.isInvalid(address, datasetService)) {
        return true;
      }
    }
    return false;
  }

  enum IPSchema {
    V4("ipv4_address.json"),
    V6("ipv6_address.json");

    private final String value;

    IPSchema(String value) {
      this.value = value;
    }

    public String path() {
      return "profile/tig_section/" + value;
    }
  }

  static class IPValidator {
    boolean isInvalid(InetAddress ipAddress, RDAPDatasetService datasetService) {
      IPSchema schema;
      if (ipAddress instanceof Inet4Address) {
        schema = IPSchema.V4;
      } else if (ipAddress instanceof Inet6Address) {
        schema = IPSchema.V6;
      } else {
        return true;
      }

      String ipAddressJson = String.format("{\"ip\": \"%s\"}", ipAddress.getHostAddress());
      SchemaValidator validator = new SchemaValidator(schema.path(),  RDAPValidatorResultsImpl.getInstance(),
          datasetService);
      boolean isValid = validator.validate(ipAddressJson);
      logger.info("IP address  {} is {} according to the schema {}", ipAddress.getHostAddress(), isValid ? "VALID" : "<INVALID>", schema.path());
      return !isValid;
    }
  }
}
