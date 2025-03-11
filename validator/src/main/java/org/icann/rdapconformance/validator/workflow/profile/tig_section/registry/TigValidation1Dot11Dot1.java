package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TigValidation1Dot11Dot1 extends ProfileValidation {

  private final static Logger logger = LoggerFactory.getLogger(TigValidation1Dot11Dot1.class);
  private final RDAPValidatorConfiguration config;
  private final RDAPDatasetService datasetService;
  private final RDAPQueryType queryType;

  public TigValidation1Dot11Dot1(RDAPValidatorConfiguration config,
      RDAPValidatorResults results, RDAPDatasetService datasetService,
      RDAPQueryType queryType) {
    super(results);
    this.config = config;
    this.datasetService = datasetService;
    this.queryType = queryType;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_11_1_Validation";
  }

  @Override
  public boolean doValidate() {
    boolean isValid = true;

    BootstrapDomainNameSpace dataset = datasetService.get(BootstrapDomainNameSpace.class);
    String urlWithoutPort = removePortInURL();
    String tld = urlWithoutPort
        .substring(urlWithoutPort.lastIndexOf(".") + 1);

    if (!dataset.tldExists(tld)) {
      results.add(RDAPValidationResult.builder()
          .code(-23100)
          .value(
              tld + "\n/\n" + dataset.getTlds().stream().sorted().collect(Collectors.joining(", ")))
          .message("The TLD is not included in the bootstrapDomainNameSpace. "
              + "See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1.")
          .build());
      isValid = false;
    } else {
      Set<String> urls = dataset.getUrlsForTld(tld);
      if (StringUtils.isNoneBlank(urlWithoutPort) && urls.stream().noneMatch(urlWithoutPort::startsWith)) {
        results.add(RDAPValidationResult.builder()
            .code(-23101)
            .value(urls.stream().sorted().collect(Collectors.joining(", ")))
            .message("The TLD entry in bootstrapDomainNameSpace does not contain a base URL. "
                + "See section 1.11.1 of the RDAP_Technical_Implementation_Guide_2_1.")
            .build());
        isValid = false;
      }
      if (urls.stream().anyMatch(u -> !URI.create(u).getScheme().equals("https"))) {
        results.add(RDAPValidationResult.builder()
            .code(-23102)
            .value(urls.stream().sorted().collect(Collectors.joining(", ")))
            .message("One or more of the base URLs for the TLD contain a schema different from "
                + "https. See section 1.2 of the RDAP_Technical_Implementation_Guide_2_1.")
            .build());
        isValid = false;
      }
    }

    return isValid;
  }

  @Override
  public boolean doLaunch() {
    return config.isGtldRegistry() && queryType.equals(RDAPQueryType.DOMAIN);
  }

  private String removePortInURL() {
    URI uri = URI.create(config.getUri().toString());
    try {
      uri = new URI(uri.getScheme(), uri.getHost(), uri.getPath(), uri.getFragment());
    } catch (URISyntaxException e) {
      logger.error("URI has a syntax issue: ", e);
      return StringUtils.EMPTY;
    }
    return uri.toString();
  }
}
