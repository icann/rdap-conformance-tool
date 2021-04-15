package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues;

public class RDAPJsonValuesDataset extends RDAPDataset<RDAPJsonValues> {

  public RDAPJsonValuesDataset(FileSystem fileSystem) {
    super("RDAPJSONValues",
        URI.create("https://www.iana.org/assignments/rdap-json-values/rdap-json-values.xml"),
        fileSystem, RDAPJsonValues.class);
  }
}
