package org.icann.rdapconformance.validator.configuration;

import com.ibm.icu.text.IDNA;
import java.net.URI;
import java.net.URISyntaxException;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface RDAPValidatorConfiguration {

  Logger logger = LoggerFactory.getLogger(RDAPValidatorConfiguration.class);

  URI getConfigurationFile();

  URI getUri();

  void setUri(URI uri);

  int getTimeout();

  int getMaxRedirects();

  boolean useLocalDatasets();

  boolean useRdapProfileFeb2019();
  boolean useRdapProfileFeb2024();

  boolean isGtldRegistrar();

  boolean isGtldRegistry();

  boolean isThin();

  String getResultsFile();

  boolean isNoIpv4Queries();

  RDAPQueryType getQueryType();

  boolean isNoIpv6Queries();

  boolean isNetworkEnabled();
  boolean isAdditionalConformanceQueries();

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

    if (isGtldRegistrar() && isGtldRegistry()) {
      logger.error("gTLD cannot be a registrar and a registry at the same time");
      return false;
    }
    if (isThin() && !isGtldRegistry()) {
      logger.error("Thin only applies for gTLD registry");
      return false;
    }
    if (useRdapProfileFeb2019() && !(isGtldRegistry() || isGtldRegistrar())) {
      logger.error("RDAP profile February 2019 need gTLD type to be specified");
      return false;
    }

    // transform URI host from U-label to A-label if necessary, ignore errors
    if (null == getUri().getHost() && null != getUri().getAuthority()) {
      // for U-label, URI host is null
      IDNA idna = IDNA.getUTS46Instance(IDNA.NONTRANSITIONAL_TO_ASCII
          | IDNA.NONTRANSITIONAL_TO_UNICODE
          | IDNA.CHECK_BIDI
          | IDNA.CHECK_CONTEXTJ
          | IDNA.CHECK_CONTEXTO
          | IDNA.USE_STD3_RULES);
      IDNA.Info info = new IDNA.Info();
      StringBuilder asciiHost = new StringBuilder();
      idna.nameToASCII(
          getUri().getAuthority().substring(0, getUri().getAuthority().lastIndexOf(":")), asciiHost,
          info);
      if (!info.hasErrors()) {
        try {
          URI new_uri = new URI(getUri().getScheme(), getUri().getUserInfo(), asciiHost.toString(),
              getUri().getPort(), getUri().getPath(), getUri().getQuery(), getUri().getFragment());
          setUri(new_uri);
        } catch (URISyntaxException e) {
          logger.error("Failed to transform URI host from U-Label to A-Label");
        }
      }
    }

    return true;
  }

  void clean();
}
