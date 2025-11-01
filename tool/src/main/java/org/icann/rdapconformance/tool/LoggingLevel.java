package org.icann.rdapconformance.tool;

/**
 * Enum defining logging levels for the RDAP Conformance Tool.
 *
 * <p>This enum provides different logging configurations to support both CLI usage
 * and library integration scenarios, with careful consideration for progress bar
 * compatibility and root logger isolation.</p>
 */
public enum LoggingLevel {
    /**
     * CLI mode (default) - Shows progress bar with minimal logging.
     * Progress bar compatible: Root=ERROR, rdapconformance=ERROR.
     * This is the default mode for command-line usage.
     */
    CLI,

    /**
     * INFO mode - Shows INFO level logs for rdapconformance package only.
     * Progress bar disabled: Root=ERROR, rdapconformance=INFO.
     * Useful for monitoring and getting insight into validation process.
     */
    INFO,

    /**
     * DEBUG mode - Shows DEBUG level logs for rdapconformance package only.
     * Progress bar disabled: Root=ERROR, rdapconformance=DEBUG.
     * Ideal for troubleshooting specific validation issues.
     */
    DEBUG,

    /**
     * ERROR mode - Shows only ERROR level logs.
     * Progress bar disabled: Root=ERROR, rdapconformance=ERROR.
     * Minimal output for production use.
     */
    ERROR,

    /**
     * VERBOSE mode - Shows DEBUG level logs for all packages (legacy -v behavior).
     * Progress bar disabled: Root=DEBUG, rdapconformance=DEBUG.
     * Maximum debugging information, equivalent to the legacy -v flag.
     */
    VERBOSE
}