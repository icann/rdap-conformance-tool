package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

public class RdapToolExample {

    public static void main(String[] args) throws Exception {
        // Create a unique session ID for this validation run
        // This is especially important for concurrent environments (web apps, services)
        String sessionId = UUID.randomUUID().toString();
        System.out.println("Starting validation with session ID: " + sessionId);

        RdapConformanceTool tool = new RdapConformanceTool();

        // Set the session ID BEFORE configuring and running the tool
        // This ensures that all validation results are properly isolated to this session
        tool.setSessionId(sessionId);

        // Required parameters
        tool.setUri(URI.create("https://rdap.arin.net/registry/entity/GOGL"));
        tool.setTimeout(5);
        tool.setMaxRedirects(2);
        tool.setUseLocalDatasets(false);
        tool.setResultsFile("/tmp/results.json");
        tool.setExecuteIPv4Queries(true);
        tool.setExecuteIPv6Queries(false);
        tool.setAdditionalConformanceQueries(true);
        tool.setVerbose(false);
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

        // Clean up this specific session when done
        // This is crucial for long-running applications to prevent memory leaks
        // and session cross-contamination in concurrent environments
        System.out.println("\nCleaning up session: " + sessionId);
        tool.clean(sessionId);

        System.out.println("\n=== SESSION MANAGEMENT NOTES ===");
        System.out.println("This example demonstrates external session management:");
        System.out.println("1. Create unique session ID before tool setup");
        System.out.println("2. Set session ID on tool before calling validation");
        System.out.println("3. All results are isolated to this session");
        System.out.println("4. Clean up specific session when done");
        System.out.println("");
        System.out.println("Benefits:");
        System.out.println("- Prevents race conditions in concurrent environments");
        System.out.println("- Avoids session cross-contamination in web applications");
        System.out.println("- Allows selective cleanup without affecting other sessions");
        System.out.println("- Enables proper resource management in long-running services");
    }
}