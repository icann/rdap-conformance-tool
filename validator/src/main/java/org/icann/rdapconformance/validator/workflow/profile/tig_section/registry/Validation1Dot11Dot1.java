package org.icann.rdapconformance.validator.workflow.profile.tig_section.registry;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.tig_section.TigValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;

public class Validation1Dot11Dot1 extends TigValidation {


  private final RDAPValidatorConfiguration config;
  private final RDAPDatasetService datasetService;

  public Validation1Dot11Dot1(RDAPValidatorConfiguration config,
      RDAPValidatorResults results, RDAPDatasetService datasetService) {
    super(results);
    this.config = config;
    this.datasetService = datasetService;
  }

  @Override
  public String getGroupName() {
    return "tigSection_1_11_1_Validation";
  }

  @Override
  public boolean doValidate() {
    boolean isValid = true;

    BootstrapDomainNameSpace dataset = datasetService.get(BootstrapDomainNameSpace.class);
    String tld = config.getUri().toString()
        .substring(config.getUri().toString().lastIndexOf(".") + 1);

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
      if (urls.stream().noneMatch(u -> config.getUri().toString().startsWith(u))) {
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
}
