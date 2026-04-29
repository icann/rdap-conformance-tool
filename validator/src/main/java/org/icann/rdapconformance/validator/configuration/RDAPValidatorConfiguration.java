package org.icann.rdapconformance.validator.configuration;

import com.ibm.icu.text.IDNA;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

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

  /**
   * Returns custom dataset directory path for IANA XML files.
   * If null, uses the default "datasets" directory.
   * @return custom dataset directory path, or null for default
   */
  default String getDatasetDirectory() {
    return null;
  }


  /**
   * Returns the list of hosts or IP addresses that are explicitly exempted from
   * SSRF (Server-Side Request Forgery) protection checks.
   *
   * <p>Entries in this list bypass the private/reserved IP address validation
   * performed in both the initial connection check and redirect destination
   * validation. This allows specific internal or test servers to be reached
   * even when their resolved IP addresses fall within ranges that would
   * otherwise be blocked (e.g., RFC 1918 private IPv4, ULA IPv6, loopback).</p>
   *
   * <p><strong>Accepted value formats:</strong></p>
   * <ul>
   *   <li><strong>Hostnames</strong> — matched case-insensitively against the
   *       request URI host (e.g., {@code ts-wire-mock.icann.org})</li>
   *   <li><strong>IPv4 literals</strong> — matched against the resolved remote
   *       address (e.g., {@code 10.47.230.173})</li>
   *   <li><strong>IPv6 literals</strong> — matched against the resolved remote
   *       address in full expanded or compressed form
   *       (e.g., {@code 2620:0:2830:270:0:0:0:173})</li>
   * </ul>
   *
   * <p><strong>Matching behavior:</strong> An entry matches if it equals either
   * the resolved IP address ({@code InetAddress.getHostAddress()}) or the
   * lowercase hostname extracted from the request URI. No CIDR range matching
   * or wildcard expansion is performed — values must be exact.</p>
   *
   * <p><strong>Cross-family note:</strong> Allowlisting a hostname permits
   * connections on <em>both</em> IPv4 and IPv6 for that host. Allowlisting a
   * specific IP literal permits only that exact address.</p>
   *
   * @return an unmodifiable list of hostname or IP literal strings to exempt
   *         from SSRF validation; never {@code null}; empty by default
   * @see org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest
   */
  default List<String> getSsrfAllowedHosts() {
    return Collections.emptyList();
  }

  /**
   * Whether to cleanup dataset files after validation completes.
   * Only applies when using a custom dataset directory.
   * @return true to cleanup datasets after validation, false to keep them
   */
  default boolean isCleanupDatasetsOnComplete() {
    return false;
  }

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
          logger.debug("Failed to transform URI host from U-Label to A-Label");
        }
      }
    }

    return true;
  }

  void clean();
}
