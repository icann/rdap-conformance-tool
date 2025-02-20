package org.icann.rdapconformance.validator.customvalidator;

import inet.ipaddr.HostName;
import inet.ipaddr.IPAddress.IPVersion;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;

public class HostNameInUriFormatValidator implements FormatValidator {

  private final Ipv4FormatValidator ipv4FormatValidator;
  private final Ipv6FormatValidator ipv6FormatValidator;
  private final RDAPValidatorConfiguration config;

  public HostNameInUriFormatValidator(Ipv4FormatValidator ipv4FormatValidator,
      Ipv6FormatValidator ipv6FormatValidator, RDAPValidatorConfiguration config) {
    this.ipv4FormatValidator = ipv4FormatValidator;
    this.ipv6FormatValidator = ipv6FormatValidator;
    this.config = config;
  }

  @Override
  public Optional<String> validate(String subject) {
    try {
      URI uri = new URI(subject);
      if (uri.getAuthority() == null) {
        return Optional.of("Can't parse the hostname of the URI " + uri);
      }
      HostName hostName = new HostName(uri.getRawAuthority());
      if (hostName.isAddress(IPVersion.IPV4) && !config.isNoIPV4Queries()) {
        return ipv4FormatValidator.validate(hostName.getHost());
      } else if (hostName.isAddress(IPVersion.IPV6)) {
        return ipv6FormatValidator.validate(hostName.getHost());
      }

      return new IdnHostNameFormatValidator().validate(uri.getRawAuthority());
    } catch (URISyntaxException e) {
      return Optional.of(e.getMessage());
    }
  }

  @Override
  public String formatName() {
    return "hostname-in-uri";
  }
}
