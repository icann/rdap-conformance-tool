package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DsRrTypes;

public class DsRrTypesDataset extends RDAPDataset<DsRrTypes> {

  public DsRrTypesDataset(FileSystem fileSystem) {
    super("dsRrTypes",
        URI.create("https://www.iana.org/assignments/ds-rr-types/ds-rr-types.xml"),
        fileSystem, DsRrTypes.class);
  }
}
