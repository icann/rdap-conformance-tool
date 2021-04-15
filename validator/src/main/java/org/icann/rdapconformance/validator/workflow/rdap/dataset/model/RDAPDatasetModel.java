package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import java.io.InputStream;

public interface RDAPDatasetModel {

  void parse(InputStream inputStream) throws Throwable;
}
