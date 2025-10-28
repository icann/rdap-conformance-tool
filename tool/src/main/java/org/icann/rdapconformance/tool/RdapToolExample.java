package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RdapToolExample {

    public static void main(String[] args) throws Exception {
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
        // Use Path API for safer file path handling
        Path currentDir = Paths.get(System.getProperty("user.dir"));
        Path configFilePath;
        if (currentDir.getFileName().toString().equals("tool")) {
            configFilePath = currentDir.resolve("bin/rdapct_config.json");
        } else {
            configFilePath = currentDir.resolve("tool/bin/rdapct_config.json");
        }
        tool.setConfigurationFile(configFilePath.toString());

        // Set gTLD/Registrar/Registry/Profile options as needed
        // Example: enable RDAP Profile Feb 2024
        tool.setUseRdapProfileFeb2024(true);
        // Example: set as gTLD registry
        tool.setGtldRegistry(true);

        // Run the tool
        int result = tool.call();
        System.out.println("Exit code: " + result);
        
        // Get and display validation errors
        List<RDAPValidationResult> errors = tool.getErrors();
        System.out.println("\n=== VALIDATION ERRORS ===");
        System.out.println("Number of errors: " + tool.getErrorCount());
        
        if (!errors.isEmpty()) {
            System.out.println("\nError details:");
            for (int i = 0; i < errors.size(); i++) {
                RDAPValidationResult error = errors.get(i);
                System.out.println("\nError " + (i + 1) + ":");
                System.out.println("  Code: " + error.getCode());
                System.out.println("  Message: " + error.getMessage());
                System.out.println("  Value: " + (error.getValue() != null ? error.getValue() : "N/A"));
                System.out.println("  HTTP Status: " + (error.getHttpStatusCode() != null ? error.getHttpStatusCode() : "N/A"));
                System.out.println("  Queried URI: " + (error.getQueriedURI() != null ? error.getQueriedURI() : "N/A"));
            }
        } else {
            System.out.println("No validation errors found!");
        }
        
        // Display total results count
        List<RDAPValidationResult> allResults = tool.getAllResults();
        System.out.println("\n=== SUMMARY ===");
        System.out.println("Total validation results: " + allResults.size());
        System.out.println("Errors: " + tool.getErrorCount());
        System.out.println("Warnings/Other: " + (allResults.size() - tool.getErrorCount()));
        
        // Demonstrate JSON output methods
        System.out.println("\n=== JSON OUTPUT EXAMPLES ===");
        
        // Get first 3 errors as JSON (to keep output manageable)
        String errorsJson = tool.getErrorsAsJson();
        System.out.println("\nFirst few errors as JSON:");
        if (!errorsJson.equals("[]")) {
            // Parse and show only first few errors to keep output readable
            System.out.println(errorsJson.substring(0, Math.min(500, errorsJson.length())) + 
                              (errorsJson.length() > 500 ? "...\n(truncated for readability)" : ""));
        } else {
            System.out.println(errorsJson);
        }
        
        // Get warnings as JSON
        String warningsJson = tool.getWarningsAsJson();
        System.out.println("\nWarnings as JSON:");
        System.out.println(warningsJson.substring(0, Math.min(300, warningsJson.length())) + 
                          (warningsJson.length() > 300 ? "...\n(truncated for readability)" : ""));
        
        // Show complete structure (just metadata, not full content)
        String allResultsJson = tool.getAllResultsAsJson();
        System.out.println("\nComplete results structure available via getAllResultsAsJson()");
        System.out.println("Contains: errors, warnings, ignore list, and notes");
        System.out.println("Full JSON size: " + allResultsJson.length() + " characters");
    }
}