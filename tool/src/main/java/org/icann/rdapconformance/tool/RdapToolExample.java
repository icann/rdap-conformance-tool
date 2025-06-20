import org.icann.rdapconformance.tool.RdapConformanceTool;
import java.net.URI;

public class RdapToolExample {
    public static void main(String[] args) throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();

        // Required parameters
        tool.setUri(URI.create("https://rdap.arin.net/registry/entity/GOGL"));
        tool.configurationFile = "/tmp/rdapct_config.json";

        // Optional parameters
        tool.timeout = 5;
        tool.maxRedirects = 2;
        tool.useLocalDatasets = false;
        tool.resultsFile = "/tmp/results.json";
        tool.executeIPv4Queries = true;
        tool.executeIPv6Queries = false;
        tool.additionalConformanceQueries = true;
        tool.isVerbose = true;

        // Set gTLD/Registrar/Registry/Profile options as needed
        // Example: enable RDAP Profile Feb 2024
        tool.dependantRdapProfileGtld.exclusiveRdapProfile.dependantRdapProfile.useRdapProfileFeb2024 = true;
        // Example: set as gTLD registry
        tool.dependantRdapProfileGtld.exclusiveRdapProfile.exclusiveGtldType.dependantRegistryThin.gtldRegistry = true;

        // Run the tool
        int result = tool.call();
        System.out.println("Exit code: " + result);
    }
}