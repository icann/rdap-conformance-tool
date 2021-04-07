package org.icann.rdapconformance.validator;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.icann.rdapconformance.validator.configuration.ConfigurationFile;
import org.icann.rdapconformance.validator.configuration.ConfigurationFileParser;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RDAPValidator {

  private static final Logger logger = LoggerFactory.getLogger(RDAPValidator.class);

  private final RDAPValidatorConfiguration config;


  public RDAPValidator(RDAPValidatorConfiguration config) {
    this.config = config;
    if (!this.config.check()) {
      logger.error("Please fix the configuration");
      throw new RuntimeException("Please fix the configuration");
    }
  }

  public int validate() {
    /* Parse the configuration definition file, and if the file is not parsable,
     * exit with a return code of 1.
     */
    ConfigurationFile configurationFile;
    try {
      ConfigurationFileParser configParser = new ConfigurationFileParser();
      configurationFile = configParser.parse(this.config.getConfigurationFile());
    } catch (Exception e) {
      logger.error("Configuration is invalid", e);
      return RDAPValidationStatus.CONFIG_INVALID.getValue();
    }

    RDAPValidatorContext context = new RDAPValidatorContext(configurationFile);

    /* If the parameter (--use-local-datasets) is set, use the datasets found in the filesystem,
     * download the datasets not found in the filesystem, and persist them in the filesystem.
     * If the parameter (--use-local-datasets) is not set, download all the datasets, and
     * overwrite the datasets in the filesystem.
     * If one or more datasets cannot be downloaded, exit with a return code of 2.
     */
    /* TODO */

    /* Verify the URI represent one of the following RDAP queries, and if not, exit with a return
     * code of 3:
     *  * domain/<domain name>
     *    Note: the domain name must pass Domain Name validation [domainNameValidation], however,
     *    A-labels and U-labels (see IDNA_RFCs) shall not be mixed. If A-labels and U-labels are
     *    mixed, the tool shall exit with the return code of 4.
     *  * nameserver/<nameserver name>
     *    Note: the nameserver name must pass Domain Name validation [domainNameValidation],
     *    however A-labels and U-labels (see IDNA_RFCs) shall not be mixed. If A-labels and
     *    U-labels are mixed, the tool shall exit with the return code of 4.
     *  * entity/<handle>
     *  * help
     *  * nameservers?ip=<nameserver search pattern>
     */
    RDAPQueryType queryType = RDAPQueryType.getType(this.config.getUri().toString());
    if (queryType == null) {
      logger.error("Unknown RDAP query type for URI {}", this.config.getUri());
      return RDAPValidationStatus.UNSUPPORTED_QUERY.getValue();
    }
    if (Set.of(RDAPQueryType.DOMAIN, RDAPQueryType.NAMESERVER).contains(queryType)) {
      String domainName = queryType.getValue(this.config.getUri().toString());
      String domainNameJson = String.format("{\"domain\": \"%s\"}", domainName);
      SchemaValidator validator = new SchemaValidator("rdap_domain_name.json", context);
      if (!validator.validate(domainNameJson)) {
        // TODO check if A-labels and U-labels are mixed: is this OK?
        if (context.getResults().stream().map(RDAPValidationResult::getCode)
            .anyMatch(c -> c == -10303)) {
          return RDAPValidationStatus.MIXED_LABEL_FORMAT.getValue();
        }
      }
    }


    /* Connect to the RDAP server, and if there is a connection error, exit with the corresponding
     * return code.
     */
    HttpResponse<String> httpResponse;
    try {
      httpResponse = getHttpResponse();
    } catch (RDAPHttpException e) {
      return e.getStatus();
    }

    HttpHeaders headers = httpResponse.headers();
    String rdapResponse = httpResponse.body();
    int httpStatusCode = httpResponse.statusCode();

    /* If a response is available to the tool, and the header Content-Type is not
     * application/rdap+JSON, exit with a return code of 5.
     */
    if (Arrays.stream(String.join(";", headers.allValues("Content-Type")).split(";"))
        .noneMatch(s -> s.equalsIgnoreCase("application/rdap+JSON"))) {
      logger.error("Content-Type is {}, should be application/rdap+JSON",
          headers.firstValue("Content-Type").orElse("missing"));
      return RDAPValidationStatus.WRONG_CONTENT_TYPE.getValue();
    }

    /*  If a response is available to the tool, but it's not syntactically valid JSON object, exit
     * with a return code of 6.
     */
    JSONObject rawRdapJson = null;
    JSONArray rawRdapJsonArray = null;
    try {
      rawRdapJson = new JSONObject(rdapResponse);
    } catch (JSONException e1) {
      // JSON content may be a list
      try {
        rawRdapJsonArray = new JSONArray(rdapResponse);
      } catch (Exception e2) {
        logger.error("Invalid JSON in RDAP response");
        return RDAPValidationStatus.RESPONSE_INVALID.getValue();
      }
    }

    /* If a response is available to the tool, but the HTTP status code is not 200 nor 404, exit
     * with a return code of 7.
     */
    if (!List.of(200, 404).contains(httpStatusCode)) {
      logger.error("Invalid HTTP status {}", httpStatusCode);
      return RDAPValidationStatus.INVALID_HTTP_STATUS.getValue();
    }

    /* If a response is available to the tool, but the expected objectClassName in the topmost
     * object was not found for a lookup query (i.e. domain/<domain name>,
     * nameserver/<nameserver name> and entity/<handle>) nor the expected JSON array
     * (i.e. nameservers?ip=<nameserver search pattern>, just the JSON array should exist,
     * not validation on the contents) for a search query, exit with an return code of 8.
     */
    // TODO should we check that status is not 404 before that?
    if (queryType.isLookupQuery()
        && (null == rawRdapJson || null == rawRdapJson.opt("objectClassName"))) {
      logger.error("objectClassName was not found in the topmost object");
      return RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND.getValue();
    } else if (queryType.equals(RDAPQueryType.NAMESERVERS) && null == rawRdapJsonArray) {
      logger.error("No JSON array in answer");
      return RDAPValidationStatus.EXPECTED_OBJECT_NOT_FOUND.getValue();
    }

    /*  If the HTTP status code is 404, perform Error Response Body
     * [stdRdapErrorResponseBodyValidation].
     */

    /* If
     * the RDAP query is domain/<domain name>, perform Domain Lookup Validation
     * [stdRdapDomainLookupValidation].
     * the RDAP query is nameserver/<nameserver name>, perform Nameserver lookup validation
     * [stdRdapNameserverLookupValidation].
     * the RDAP query is entity/<handle>, perform Entity lookup validation
     * [stdRdapEntityLookupValidation] if the flag --thin is not set. If the flag --thin is set,
     * exit with a return code of 9.
     * the RDAP query is help, perform Help validation [stdRdapHelpValidation].
     * the RDAP query is nameservers?ip=<nameserver search pattern>, perform Nameservers search
     * validation [stdRdapNameserversSearchValidation].
     *
     *
     */
    context.reset();  // reset context as we may have performed some preliminary validation
    SchemaValidator validator = null;
    if (404 == httpStatusCode) {
      validator = new SchemaValidator("rdap_error.json", context);
    } else if (RDAPQueryType.DOMAIN.equals(queryType)) {
      validator = new SchemaValidator("rdap_domain.json", context);
    } else if (RDAPQueryType.HELP.equals(queryType)) {
      validator = new SchemaValidator("rdap_help.json", context);
    } else if (RDAPQueryType.NAMESERVER.equals(queryType)) {
      validator = new SchemaValidator("rdap_nameserver.json", context);
    } else if (RDAPQueryType.NAMESERVERS.equals(queryType)) {
      validator = new SchemaValidator("rdap_nameservers.json", context);
    } else if (RDAPQueryType.ENTITY.equals(queryType)) {
      validator = new SchemaValidator("rdap_entities.json", context);
    }
    assert null != validator;
    validator.validate(rdapResponse);

    // TODO create result file in context

    /*
     * Additionally, apply the relevant collection tests when the option
     * --use-rdap-profile-february-2019 is set.
     */
    /* TODO */

    return RDAPValidationStatus.SUCCESS.getValue();
  }

  HttpResponse<String> getHttpResponse() throws RDAPHttpException {
    /* If the scheme of the URI is "https", the tool will initiate a TLS connection to the server:
     *  • The tool shall not try to match CA certs (if available) to a well-known CA.
     *  • If the CRL or OCSP is unavailable, this won't constitute an error, but if CRL or OCSP are
     *    accessible and indicate that the server certificate is revoked, the revocation
     *    constitutes an error.
     */
    final URI uri = this.config.getUri();
    if (uri.getScheme().equals("https")) {
//      System.setProperty("jdk.security.allowNonCaAnchor", String.valueOf(true));
      System.setProperty("com.sun.net.ssl.checkRevocation", String.valueOf(true));
    }

    TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          public void checkClientTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }

          public void checkServerTrusted(
              java.security.cert.X509Certificate[] certs, String authType) {
          }
        }
    };
    SSLContext sslContext;
    try {
      sslContext = SSLContext.getInstance("SSL");
      sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new RDAPHttpException(RDAPValidationStatus.CERTIFICATE_ERROR, e);
    }

    System.setProperty("jdk.httpclient.redirects.retrylimit",
        String.valueOf(this.config.getMaxRedirects()));
    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.of(this.config.getTimeout(), SECONDS))
        .GET()
        .build();
    HttpResponse<String> httpResponse;
    RDAPValidationStatus status;
    Throwable ex;
    try {
      httpResponse = HttpClient.newBuilder()
          .connectTimeout(Duration.of(this.config.getTimeout(), SECONDS))
          .followRedirects(Redirect.NORMAL)
          .sslContext(sslContext)
          .build()
          .send(request, HttpResponse.BodyHandlers.ofString());
      if (String.valueOf(httpResponse.statusCode()).startsWith("30")) {
          throw new RDAPHttpException(RDAPValidationStatus.TOO_MANY_REDIRECTS, null);
      }
      return httpResponse;
    } catch (ConnectException | HttpTimeoutException e) {
      ex = e;
      status = RDAPValidationStatus.CONNECTION_FAILED;
      if (hasCause(e, "UnresolvedAddressException")) {
        status = RDAPValidationStatus.NETWORK_SEND_FAIL;
      }
    } catch (IOException e) {
      ex = e;
      status = RDAPValidationStatus.NETWORK_RECEIVE_FAIL;
      if (hasCause(e, "SSLHandshakeException") || hasCause(e, "CertificateException")) {
        status = RDAPValidationStatus.CERTIFICATE_ERROR;
      }
    } catch (Exception e) {
      ex = e;
      status = RDAPValidationStatus.CONNECTION_FAILED;
    }
    throw new RDAPHttpException(status, ex);
  }

  private boolean hasCause(Throwable e, String causeClassName) {
    while (e.getCause() != null) {
      if (e.getCause().getClass().getSimpleName().equals(causeClassName)) {
        return true;
      }
      e = e.getCause();
    }
    return false;
  }

  private enum RDAPQueryType {
    DOMAIN(Pattern.compile("/domain/([^/]+)$")),
    NAMESERVER(Pattern.compile("/nameserver/([^/]+)$")),
    ENTITY(Pattern.compile("/entity/([^/]+)$")),
    HELP(Pattern.compile("/help$")),
    NAMESERVERS(Pattern.compile("/nameservers?ip=([^/]+)$"));

    private final Pattern pattern;

    RDAPQueryType(Pattern pattern) {
      this.pattern = pattern;
    }

    static RDAPQueryType getType(String query) {
      for (RDAPQueryType qt : RDAPQueryType.values()) {
        Matcher matcher = qt.pattern.matcher(query);
        if (matcher.find()) {
          return qt;
        }
      }
      return null;
    }

    public boolean isLookupQuery() {
      return Set.of(RDAPQueryType.DOMAIN, RDAPQueryType.NAMESERVER, RDAPQueryType.ENTITY)
          .contains(this);
    }

    public String getValue(String query) {
      Matcher matcher = this.pattern.matcher(query);
      if (matcher.find()) {
        return matcher.group(1);
      }
      return "";
    }
  }

  static class RDAPHttpException extends Throwable {

    private final RDAPValidationStatus status;

    public RDAPHttpException(RDAPValidationStatus status, Throwable e) {
      super(status.getDescription(), e);
      this.status = status;
    }

    public int getStatus() {
      return status.getValue();
    }
  }
}
