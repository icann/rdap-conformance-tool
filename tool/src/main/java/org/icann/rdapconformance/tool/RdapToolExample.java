package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RdapToolExample {

    private static final String NOT_AVAILABLE = "N/A";
    private static final String TRUNCATED_SUFFIX = "...\n(truncated for readability)";

    public static void main(String[] args) {
        RdapConformanceTool tool = createConfiguredTool();

        // Run the tool
        int result;
        try {
            result = tool.call();
        } catch (Exception e) {
            // Keep failures visible: the uncaught exception prints the full stack trace
            // and the JVM exits with a non-zero status.
            throw new IllegalStateException("RDAP conformance tool failed: " + e, e);
        }
        System.out.println("Exit code: " + result);

        printErrors(tool);
        printSummary(tool);
        printJsonExamples(tool);
    }

    private static RdapConformanceTool createConfiguredTool() {
        RdapConformanceTool tool = new RdapConformanceTool();

        // Required parameters
        tool.setUri(URI.create("https://rdap.arin.net/registry/entity/GOGL"));
        tool.setTimeout(5);
        tool.setMaxRedirects(2);
        tool.setUseLocalDatasets(false);
        tool.setResultsFile("/tmp/results.json");
        tool.setExecuteIPv4Queries(true);
        tool.setExecuteIPv6Queries(false);
        tool.setAdditionalConformanceQueries(true);
        // tool.setVerbose(false);  // Legacy verbose flag
        tool.setLogging(LoggingLevel.INFO);  // New logging system: CLI, INFO, DEBUG, ERROR, VERBOSE
        tool.setConfigurationFile(resolveConfigFile().toString());

        // Set gTLD/Registrar/Registry/Profile options as needed
        // Example: enable RDAP Profile Feb 2024
        tool.setUseRdapProfileFeb2024(true);
        // Example: set as gTLD registry
        tool.setGtldRegistry(true);
        return tool;
    }

    /** Use Path API for safer file path handling. */
    private static Path resolveConfigFile() {
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        if (currentDir.getFileName().toString().equals("tool")) {
            return currentDir.resolve("bin/rdapct_config.json");
        }
        return currentDir.resolve("tool/bin/rdapct_config.json");
    }

    private static void printErrors(RdapConformanceTool tool) {
        List<RDAPValidationResult> errors = tool.getErrors();
        System.out.println("\n=== VALIDATION ERRORS ===");
        System.out.println("Number of errors: " + tool.getErrorCount());

        if (errors.isEmpty()) {
            System.out.println("No validation errors found!");
            return;
        }
        System.out.println("\nError details:");
        for (int i = 0; i < errors.size(); i++) {
            printError(i + 1, errors.get(i));
        }
    }

    private static void printError(int index, RDAPValidationResult error) {
        System.out.println("\nError " + index + ":");
        System.out.println("  Code: " + error.getCode());
        System.out.println("  Message: " + error.getMessage());
        System.out.println("  Value: " + orNotAvailable(error.getValue()));
        System.out.println("  HTTP Status: " + orNotAvailable(error.getHttpStatusCode()));
        System.out.println("  Queried URI: " + orNotAvailable(error.getQueriedURI()));
    }

    private static void printSummary(RdapConformanceTool tool) {
        List<RDAPValidationResult> allResults = tool.getAllResults();
        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total validation results: " + allResults.size());
        System.out.println("Errors: " + tool.getErrorCount());
        System.out.println("Warnings/Other: " + (allResults.size() - tool.getErrorCount()));
    }

    private static void printJsonExamples(RdapConformanceTool tool) {
        System.out.println("\n=== JSON OUTPUT EXAMPLES ===");

        // Show only the first part of each JSON to keep output readable
        System.out.println("\nFirst few errors as JSON:");
        System.out.println(truncate(tool.getErrorsAsJson(), 500));

        System.out.println("\nWarnings as JSON:");
        System.out.println(truncate(tool.getWarningsAsJson(), 300));

        // Show complete structure (just metadata, not full content)
        String allResultsJson = tool.getAllResultsAsJson();
        System.out.println("\nComplete results structure available via getAllResultsAsJson()");
        System.out.println("Contains: errors, warnings, ignore list, and notes");
        System.out.println("Full JSON size: " + allResultsJson.length() + " characters");
    }

    private static String orNotAvailable(Object value) {
        return value != null ? value.toString() : NOT_AVAILABLE;
    }

    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + TRUNCATED_SUFFIX;
    }
}