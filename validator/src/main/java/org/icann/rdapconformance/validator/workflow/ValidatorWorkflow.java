package org.icann.rdapconformance.validator.workflow;

import ch.qos.logback.classic.Level;

public interface ValidatorWorkflow {

  default int validate(boolean isVerbose) {
    if (!isVerbose) {
      ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory
          .getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
      root.setLevel(Level.INFO);
    }
    return validate();
  }

  /**
   * Validate the JSON data.
   *
   * @return The error status
   */
  int validate();

}
