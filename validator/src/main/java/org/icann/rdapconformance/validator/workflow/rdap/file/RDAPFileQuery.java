package org.icann.rdapconformance.validator.workflow.rdap.file;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQuery;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPFileQuery implements RDAPQuery {

  private static final Logger logger = LoggerFactory.getLogger(RDAPHttpQuery.class);

  private final RDAPValidatorConfiguration config;
  private final FileSystem fs = new LocalFileSystem();
  private String data;

  public RDAPFileQuery(RDAPValidatorConfiguration config) {
    this.config = config;
  }

  @Override
  public RDAPValidationStatus getErrorStatus() {
    return RDAPValidationStatus.CONFIG_INVALID;
  }

  @Override
  public boolean run() {
    final URI uri = this.config.getUri();

    try {
      data = fs.readFile(uri.getPath());
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
}
