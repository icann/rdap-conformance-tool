package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.tool.RdapConformanceTool;
import java.net.URI;

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
        tool.setVerbose(true);
        tool.setConfigurationFile("/tmp/rdapct_config.json");

        // Set gTLD/Registrar/Registry/Profile options as needed
        // Example: enable RDAP Profile Feb 2024
        tool.setUseRdapProfileFeb2024(true);
        // Example: set as gTLD registry
        tool.setGtldRegistry(true);

        // Run the tool
        int result = tool.call();
        System.out.println("Exit code: " + result);
    }
}