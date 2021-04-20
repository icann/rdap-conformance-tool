package org.icann.rdapconformance.validator.configuration;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface RDAPValidatorConfiguration {

  Logger logger = LoggerFactory.getLogger(RDAPValidatorConfiguration.class);

  URI getConfigurationFile();

  URI getUri();

  int getTimeout();

  int getMaxRedirects();

  boolean useLocalDatasets();

  boolean userRdapProfileFeb2019();

  boolean isGltdRegistrar();

  boolean isGtldRegistry();

  boolean isThin();

  RDAPQueryType getQueryType();

  default boolean check() {
    if (getUri().getScheme() != null && getUri().getScheme().startsWith("http")) {
      if (getQueryType() != null) {
        logger.error("Cannot specify query type with HTTP or HTTPs URI");
        return false;
      }
    } else if (getQueryType() == null) {
      logger.error("Please specify query type");
      return false;
    }

    if (isGltdRegistrar() && isGtldRegistry()) {
      logger.error("gTLD cannot be a registrar and a registry at the same time");
      return false;
    }
    if (isThin() && !isGtldRegistry()) {
      logger.error("Thin only applies for gTLD registry");
      return false;
    }
    if (userRdapProfileFeb2019() && !(isGtldRegistry() || isGltdRegistrar())) {
      logger.error("RDAP profile February 2019 need gTLD type to be specified");
    }
    return true;
  }
}
