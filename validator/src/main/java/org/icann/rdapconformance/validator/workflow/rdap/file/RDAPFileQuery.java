package org.icann.rdapconformance.validator.workflow.rdap.file;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPFileQuery implements RDAPQuery {

  private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQuery.class);

  private final RDAPValidatorConfiguration config;
  private final FileSystem fileSystem;
  private String data;

  public RDAPFileQuery(RDAPValidatorConfiguration config,
      FileSystem fileSystem) {
    this.config = config;
    this.fileSystem = fileSystem;
  }

  @Override
  public RDAPValidationStatus getErrorStatus() {
    return RDAPValidationStatus.CONFIG_INVALID;
  }

  @Override
  public boolean run() {
    final URI uri = this.config.getUri();
    try {
      data = fileSystem.readFile(uri);
    } catch (IOException e) {
      logger.error("Cannot read from uri {}", uri, e);
      return false;
    }
    return true;
  }

  @Override
  public Optional<Integer> getStatusCode() {
    return Optional.of(200);
  }

  @Override
  public boolean checkWithQueryType(RDAPQueryType queryType) {
    return true;
  }

  @Override
  public boolean isErrorContent() {
    return config.getQueryType().equals(RDAPQueryType.ERROR);
  }

  @Override
  public String getData() {
    return this.data;
  }

  @Override
  public Object getRawResponse() {
    return null;
  }

  @Override
  public void setResults(RDAPValidatorResults results) {}
}
