package org.icann.rdapconformance.validator.workflow.rdap.http;

import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidator;

/**
 * HTTP-specific implementation of RDAP validator for web-based RDAP services.
 *
 * <p>This validator extends the base RDAPValidator to provide HTTP/HTTPS-specific
 * functionality for validating RDAP services accessible over the web. It integrates
 * HTTP query capabilities with the standard RDAP validation framework to test
 * compliance with RDAP specifications over HTTP transport.</p>
 *
 * <p>The validator automatically handles:</p>
 * <ul>
 *   <li>HTTP/HTTPS protocol selection and configuration</li>
 *   <li>Network protocol switching (IPv4/IPv6) for dual-stack testing</li>
 *   <li>SSL/TLS certificate validation according to RDAP requirements</li>
 *   <li>HTTP header validation including Content-Type and Accept headers</li>
 *   <li>Integration with dataset services for bootstrap validation</li>
 * </ul>
 *
 * <p>This validator is the primary choice for testing public RDAP services
 * accessible over the internet, as opposed to file-based or other transport
 * mechanisms.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * RDAPValidatorConfiguration config = // ... configuration setup
 * RDAPDatasetService datasetService = // ... dataset initialization
 * RDAPHttpValidator validator = new RDAPHttpValidator(config, datasetService);
 * boolean success = validator.validate();
 * </pre>
 *
 * @see RDAPValidator
 * @see RDAPHttpQuery
 * @see RDAPValidatorConfiguration
 * @since 1.0.0
 */
public class RDAPHttpValidator extends RDAPValidator {

  /**
   * Creates a new HTTP-based RDAP validator with the specified configuration and dataset service.
   *
   * <p>This constructor initializes the validator with an HTTP query handler and connects
   * it to the provided dataset service for bootstrap and validation data. The HTTP query
   * handler will use the configuration settings for timeouts, redirects, and other
   * HTTP-specific parameters.</p>
   *
   * @param config the validator configuration containing HTTP settings, URIs, and validation options
   * @param datasetService the dataset service providing access to RDAP bootstrap and validation data
   */
  public RDAPHttpValidator(RDAPValidatorConfiguration config, RDAPDatasetService datasetService) {
    super(QueryContext.create(config, datasetService, new RDAPHttpQuery(config)));
  }
}
