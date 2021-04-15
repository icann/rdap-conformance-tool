package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DNSSecAlgNumbers;

public class DNSSecAlgNumbersDataset extends RDAPDataset<DNSSecAlgNumbers> {

    public DNSSecAlgNumbersDataset(FileSystem fileSystem) {
      super("dnsSecAlgNumbers",
          URI.create(
              "https://www.iana.org/assignments/dns-sec-alg-numbers/dns-sec-alg-numbers.xml"),
          fileSystem, DNSSecAlgNumbers.class);
    }
  }
