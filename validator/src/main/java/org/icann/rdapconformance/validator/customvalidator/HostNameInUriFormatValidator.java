package org.icann.rdapconformance.validator.customvalidator;

import inet.ipaddr.HostName;
import inet.ipaddr.IPAddress.IPVersion;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import org.everit.json.schema.FormatValidator;
import org.everit.json.schema.internal.IPV6Validator;

public class HostNameInUriFormatValidator implements FormatValidator {

  private final Ipv4FormatValidator ipv4FormatValidator;

  public HostNameInUriFormatValidator(Ipv4FormatValidator ipv4FormatValidator) {
    this.ipv4FormatValidator = ipv4FormatValidator;
  }

  @Override
  public Optional<String> validate(String subject) {
    try {
      URI uri = new URI(subject);
      if (uri.getAuthority() == null) {
        return Optional.of("Can't parse the hostname of the URI " + uri);
      }
      HostName hostName = new HostName(uri.getRawAuthority());
      if (hostName.isAddress(IPVersion.IPV4)) {
        return ipv4FormatValidator.validate(hostName.getHost());
      } else if (hostName.isAddress(IPVersion.IPV6)) {
        return new IPV6Validator().validate(hostName.getHost());
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
