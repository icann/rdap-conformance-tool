package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class RDAPJsonValuesDataset extends RDAPDataset {

  public RDAPJsonValuesDataset(FileSystem fileSystem) {
    super("RDAPJSONValues",
        URI.create("https://www.iana.org/assignments/rdap-json-values/rdap-json-values.xml"),
        fileSystem);
  }
}
