package org.icann.rdapconformance.validator.workflow.rdap.file;

import java.util.concurrent.ConcurrentHashMap;
import org.icann.rdapconformance.validator.ToolResult;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryTypeProcessor;

public class RDAPFileQueryTypeProcessor implements RDAPQueryTypeProcessor {

  // Session-keyed storage for concurrent validation requests
  private static final ConcurrentHashMap<String, RDAPFileQueryTypeProcessor> sessionInstances = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, RDAPValidatorConfiguration> sessionConfigs = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, ToolResult> sessionStatus = new ConcurrentHashMap<>();

  // Instance holds its session ID for accessing the correct session data
  private final String sessionId;

  // Private constructor for singleton
  private RDAPFileQueryTypeProcessor(String sessionId) {
    this.sessionId = sessionId;
  }

  /**
   * Gets the singleton instance for a specific session
   *
   * @param sessionId the session identifier
   * @return the singleton instance for this session
   */
  public static synchronized RDAPFileQueryTypeProcessor getInstance(String sessionId) {
    return sessionInstances.computeIfAbsent(sessionId, k -> {
      // Initialize session data when creating new instance
      sessionConfigs.remove(k); // Ensure clean state
      return new RDAPFileQueryTypeProcessor(k);
    });
  }

  /**
   * Gets the singleton instance (deprecated - uses default session)
   *
   * @deprecated Use getInstance(String sessionId) instead
   * @return the singleton instance for default session
   */
  @Deprecated
  public static synchronized RDAPFileQueryTypeProcessor getInstance() {
    return getInstance("default");
  }

  /**
   * Static method to get the singleton instance with configuration (deprecated)
   *
   * @deprecated Use getInstance(String sessionId) and setConfiguration(String sessionId, config) instead
   */
  @Deprecated
  public static synchronized RDAPFileQueryTypeProcessor getInstance(RDAPValidatorConfiguration config) {
    RDAPFileQueryTypeProcessor instance = getInstance("default");
    instance.setConfiguration(config);
    return instance;
  }

  /**
   * Resets the singleton instance for a specific session
   *
   * @param sessionId the session to reset
   */
  public static void reset(String sessionId) {
    sessionInstances.remove(sessionId);
    sessionConfigs.remove(sessionId);
    sessionStatus.remove(sessionId);
  }

  /**
   * Resets all sessions (primarily for testing)
   */
  public static void resetAll() {
    sessionInstances.clear();
    sessionConfigs.clear();
    sessionStatus.clear();
  }

  /**
   * Sets the configuration for a specific session
   *
   * @param sessionId the session identifier
   * @param config the validator configuration
   */
  public void setConfiguration(String sessionId, RDAPValidatorConfiguration config) {
    if (config != null) {
      sessionConfigs.put(sessionId, config);
    } else {
      sessionConfigs.remove(sessionId);
    }
  }

  /**
   * Method to set the configuration (deprecated - uses default session)
   *
   * @deprecated Use setConfiguration(String sessionId, config) instead
   */
  @Deprecated
  public void setConfiguration(RDAPValidatorConfiguration config) {
    setConfiguration("default", config);
  }

  @Override
  public boolean check(
      RDAPDatasetService datasetService) {
    return true;
  }

  /**
   * Gets the error status for a specific session
   *
   * @param sessionId the session identifier
   * @return the error status for this session
   */
  public ToolResult getErrorStatus(String sessionId) {
    return sessionStatus.get(sessionId);
  }

  /**
   * Gets the error status (deprecated - uses default session)
   *
   * @deprecated Use getErrorStatus(String sessionId) instead
   */
  @Override
  @Deprecated
  public ToolResult getErrorStatus() {
    return getErrorStatus("default");
  }

  /**
   * Gets the query type for a specific session
   *
   * @param sessionId the session identifier
   * @return the query type for this session
   */
  public RDAPQueryType getQueryType(String sessionId) {
    RDAPValidatorConfiguration config = sessionConfigs.get(sessionId);
    if (config == null) {
      throw new NullPointerException("Configuration is null for session: " + sessionId);
    }
    return config.getQueryType();
  }

  /**
   * Gets the query type (deprecated - uses default session)
   *
   * @deprecated Use getQueryType(String sessionId) instead
   */
  @Override
  @Deprecated
  public RDAPQueryType getQueryType() {
    return getQueryType("default");
  }
}