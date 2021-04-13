package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class DsRrTypesDataset extends RDAPDataset {

  public DsRrTypesDataset(FileSystem fileSystem) {
    super("dsRrTypes",
        URI.create("https://www.iana.org/assignments/ds-rr-types/ds-rr-types.xml"),
        fileSystem);
  }
}
