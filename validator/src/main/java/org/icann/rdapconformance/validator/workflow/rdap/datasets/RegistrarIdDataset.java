package org.icann.rdapconformance.validator.workflow.rdap.datasets;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;

public class RegistrarIdDataset extends RDAPDataset {

  public RegistrarIdDataset(FileSystem fileSystem) {
    super("registrarId",
        URI.create("https://www.iana.org/assignments/registrar-ids/registrar-ids.xml"),
        fileSystem);
  }
}
