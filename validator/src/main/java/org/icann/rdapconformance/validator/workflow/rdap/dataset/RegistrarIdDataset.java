package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;

public class RegistrarIdDataset extends RDAPDataset<RegistrarId> {

  public RegistrarIdDataset(FileSystem fileSystem) {
    super("registrarId",
        URI.create("https://www.iana.org/assignments/registrar-ids/registrar-ids.xml"),
        fileSystem, RegistrarId.class);
  }
}
