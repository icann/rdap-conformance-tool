package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.icann.rdapconformance.validator.DNSCacheResolver;


public final class TigValidation1Dot8 extends ProfileValidation {

  private static final Logger logger = LoggerFactory.getLogger(TigValidation1Dot8.class);
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
      responseOpt = response.previousResponse();
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
    // If we are validating over v4
    if(!config.isNoIpv4Queries()) {
      if (ipv4Addresses.isEmpty()) {
        results.add(RDAPValidationResult.builder()
                                        .code(-20400)
                                        .value(ipv4Addresses.stream()
                                                            .map(
                                                                InetAddress::getHostAddress) // gets the String representation of the IP
                                                            .sorted()
                                                            .collect(Collectors.joining(", ")))
                                        .message("The RDAP service is not provided over IPv4. See section 1.8 of the "
                                            + "RDAP_Technical_Implementation_Guide_2_1.")
                                        .build());
        isValid = false;
      }
    }

    // Use DNSCacheResolver for IPv6
    Set<InetAddress> ipv6Addresses = new HashSet<>(DNSCacheResolver.getAllV6Addresses(host));
    // If we are validating over v6
    if(!config.isNoIpv6Queries()) {
      if (ipv6Addresses.isEmpty()) {
        results.add(RDAPValidationResult.builder()
                                        .code(-20401)
                                        .value(ipv6Addresses.stream()
                                                            .map(
                                                                InetAddress::getHostAddress) // gets the String representation of the IP
                                                            .sorted()
                                                            .collect(Collectors.joining(", ")))
                                        .message("The RDAP service is not provided over IPv6. See section 1.8 of the "
                                            + "RDAP_Technical_Implementation_Guide_2_1.")
                                        .build());
        isValid = false;
      }
    }

    return isValid;
  }
}
