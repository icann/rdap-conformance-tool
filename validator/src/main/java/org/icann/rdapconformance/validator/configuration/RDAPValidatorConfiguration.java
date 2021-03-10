package org.icann.rdapconformance.validator.configuration;

import java.io.File;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface RDAPValidatorConfiguration {

  Logger logger = LoggerFactory.getLogger(RDAPValidatorConfiguration.class);

  File getConfigurationFile();

  URI getUri();

  int getTimeout();

  int getMaxRedirects();

  boolean useLocalDatasets();

  boolean userRdapProfileFeb2019();

  boolean isGltdRegistrar();

  boolean isGtldRegistry();

  boolean isThin();

  default boolean check() {
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
