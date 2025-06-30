package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.tool.RdapConformanceTool;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import java.net.URI;
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
        tool.setVerbose(false);
        // Use absolute path to config file so it works from any working directory
        String configPath = System.getProperty("user.dir");
        if (configPath.endsWith("/tool")) {
            tool.setConfigurationFile("bin/rdapct_config.json");
        } else {
            tool.setConfigurationFile("tool/bin/rdapct_config.json");
        }

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
    }
}