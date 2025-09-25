package org.icann.rdapconformance.tool;

import org.icann.rdapconformance.validator.ToolResult;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class RdapConformanceToolDnsResolverTest {

    private RdapConformanceTool tool;

    @BeforeMethod
    public void setUp() {
        tool = new RdapConformanceTool();
    }

    @Test
    public void testSetCustomDnsResolver() {
        tool.setCustomDnsResolver("8.8.8.8");
        assertThat(tool.getCustomDnsResolver()).isEqualTo("8.8.8.8");
    }

    @Test
    public void testGetCustomDnsResolver_DefaultNull() {
        assertThat(tool.getCustomDnsResolver()).isNull();
    }

    @Test
    public void testIsValidIpAddress_ValidIPv4() throws Exception {
        // Use reflection to access the private method for testing
        java.lang.reflect.Method method = RdapConformanceTool.class.getDeclaredMethod("isValidIpAddress", String.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(tool, "8.8.8.8");
        assertThat(result).isTrue();
    }

    @Test
    public void testIsValidIpAddress_ValidIPv6() throws Exception {
        // Use reflection to access the private method for testing
        java.lang.reflect.Method method = RdapConformanceTool.class.getDeclaredMethod("isValidIpAddress", String.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(tool, "2001:4860:4860::8888");
        assertThat(result).isTrue();
    }

    @Test
    public void testIsValidIpAddress_InvalidFormat() throws Exception {
        // Use reflection to access the private method for testing
        java.lang.reflect.Method method = RdapConformanceTool.class.getDeclaredMethod("isValidIpAddress", String.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(tool, "invalid.dns.server");
        assertThat(result).isFalse();
    }

    @Test
    public void testIsValidIpAddress_InvalidIPv4() throws Exception {
        // Use reflection to access the private method for testing
        java.lang.reflect.Method method = RdapConformanceTool.class.getDeclaredMethod("isValidIpAddress", String.class);
        method.setAccessible(true);
        
        boolean result = (boolean) method.invoke(tool, "300.300.300.300");
        assertThat(result).isFalse();
    }

    @Test
    public void testCommandLineParsing_WithDnsResolver() {
        CommandLine cmd = new CommandLine(tool);
        String[] args = {"--dns-resolver", "8.8.8.8", "-c", "config.json", "--gtld-registry", "--use-rdap-profile-february-2024", "https://example.com"};
        
        cmd.parseArgs(args);
        
        assertThat(tool.getCustomDnsResolver()).isEqualTo("8.8.8.8");
    }

    @Test
    public void testCommandLineParsing_WithoutDnsResolver() {
        CommandLine cmd = new CommandLine(tool);
        String[] args = {"-c", "config.json", "--gtld-registry", "--use-rdap-profile-february-2024", "https://example.com"};
        
        cmd.parseArgs(args);
        
        assertThat(tool.getCustomDnsResolver()).isNull();
    }

    @Test
    public void testCall_WithInvalidDnsResolver_ReturnsBadUserInput() throws Exception {
        // Setup tool with minimal required parameters
        tool.setCustomDnsResolver("invalid.dns.server");
        tool.setConfigurationFile("config.json");
        tool.uri = URI.create("https://example.com");
        tool.setUseRdapProfileFeb2024(true);
        tool.setGtldRegistry(true);
        
        Integer result = tool.call();
        
        assertThat(result).isEqualTo(ToolResult.BAD_USER_INPUT.getCode());
    }
}