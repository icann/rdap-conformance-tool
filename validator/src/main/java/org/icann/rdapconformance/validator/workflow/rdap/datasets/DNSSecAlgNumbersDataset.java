package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

  public class DNSSecAlgNumbersDataset extends RDAPDataset {

    public DNSSecAlgNumbersDataset(FileSystem fileSystem) {
      super("dnsSecAlgNumbers",
          URI.create(
              "https://www.iana.org/assignments/dns-sec-alg-numbers/dns-sec-alg-numbers.xml"),
          fileSystem);
    }
  }
