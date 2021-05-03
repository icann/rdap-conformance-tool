package org.icann.rdapconformance.validator.workflow.profile.tig_section.general;

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
import org.icann.rdapconformance.validator.workflow.profile.tig_section.general.Validation1Dot8.DNSQuery.DNSQueryResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class Validation1Dot8 implements TigValidation {

  private static final Logger logger = LoggerFactory.getLogger(Validation1Dot8.class);
  static DNSQuery dnsQuery = new DNSQuery();
  static IPValidator ipValidator = new IPValidator();
  private final HttpResponse<String> rdapResponse;
  private final RDAPValidatorResults results;
  private final RDAPDatasetService datasetService;

  public Validation1Dot8(HttpResponse<String> rdapResponse, RDAPValidatorResults results,
      RDAPDatasetService datasetService) {

    this.rdapResponse = rdapResponse;
    this.results = results;
    this.datasetService = datasetService;
  }

  @Override
  public boolean validate() {
    boolean hasError = false;
    Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
    while (responseOpt.isPresent()) {
      HttpResponse<String> response = responseOpt.get();
      if (!validateHost(response.uri(), results, datasetService)) {
        hasError = true;
      }
      responseOpt = response.previousResponse();
    }
    results.addGroup("tigSection_1_8_Validation", hasError);
    return !hasError;
  }

  private static boolean validateHost(URI uri, RDAPValidatorResults results,
      RDAPDatasetService datasetService) {
    boolean hasError = false;
    Name host;
    try {
      host = Name.fromString(uri.getHost());
    } catch (TextParseException e) {
      logger.error("Error when retrieving RDAP server hostname in order to check "
          + "[tigSection_1_8_Validation]", e);
      return true;
    }

    DNSQueryResult queryResult = dnsQuery.makeRequest(host, Type.A);
    if (queryResult.hasError() || containsInvalidIPAddress(queryResult.getIPAddresses(),
        datasetService)) {
      results.add(RDAPValidationResult.builder()
          .code(-20400)
          .value(queryResult.getIPAddresses().stream()
              .map(InetAddress::getHostAddress)
              .sorted()
              .collect(Collectors.joining(", ")))
          .message("The RDAP service is not provided over IPv4. See section 1.8 of the "
              + "RDAP_Technical_Implementation_Guide_2_1.")
          .build());
      hasError = true;
    }

    queryResult = dnsQuery.makeRequest(host, Type.AAAA);
    if (queryResult.hasError() || containsInvalidIPAddress(queryResult.getIPAddresses(),
        datasetService)) {
      results.add(RDAPValidationResult.builder()
          .code(-20401)
          .value(queryResult.getIPAddresses().stream()
              .map(InetAddress::getHostAddress)
              .sorted()
              .collect(Collectors.joining(", ")))
          .message("The RDAP service is not provided over IPv6. See section 1.8 of the "
              + "RDAP_Technical_Implementation_Guide_2_1.")
          .build());
      hasError = true;
    }

    return !hasError;
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

  static class DNSQuery {

    DNSQueryResult makeRequest(Name host, int type) {
      Lookup lookup = new Lookup(host, type);
      lookup.run();
      return new DNSQueryResult(lookup.getResult() != 0,
          getIPRecords(lookup));
    }

    private Set<InetAddress> getIPRecords(Lookup lookup) {
      Set<InetAddress> addresses = new HashSet<>();
      for (Record record : lookup.getAnswers()) {
        if (record instanceof ARecord) {
          addresses.add(((ARecord) record).getAddress());
        } else if (record instanceof AAAARecord) {
          addresses.add(((AAAARecord) record).getAddress());
        }
      }
      return addresses;
    }

    static class DNSQueryResult {

      private final boolean hasError;
      private final Set<InetAddress> IPRecords;

      public DNSQueryResult(boolean hasError, Set<InetAddress> IPRecords) {
        this.hasError = hasError;
        this.IPRecords = IPRecords;
      }

      public boolean hasError() {
        return hasError;
      }

      public Set<InetAddress> getIPAddresses() {
        return IPRecords;
      }
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
      SchemaValidator validator = new SchemaValidator(schema.path(), new RDAPValidatorResults(),
          datasetService);
      return !validator.validate(ipAddressJson);
    }
  }
}
