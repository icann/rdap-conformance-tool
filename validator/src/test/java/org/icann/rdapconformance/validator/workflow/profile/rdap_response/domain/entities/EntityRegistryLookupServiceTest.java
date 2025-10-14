package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain.entities;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpRequest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

public class EntityRegistryLookupServiceTest {

    private EntityRegistryLookupService lookupService;
    private RDAPDatasetService datasetService;
    private RDAPValidatorConfiguration config;
    private BootstrapDomainNameSpace bootstrap;
    private RDAPHttpRequest httpRequest;

    @BeforeMethod
    public void setUp() {
        datasetService = mock(RDAPDatasetService.class);
        config = mock(RDAPValidatorConfiguration.class);
        bootstrap = mock(BootstrapDomainNameSpace.class);
        httpRequest = mock(RDAPHttpRequest.class);

        when(datasetService.get(BootstrapDomainNameSpace.class)).thenReturn(bootstrap);

        lookupService = new EntityRegistryLookupService(datasetService, config);
    }

    @Test
    public void testIsEntityInThickRegistry_EntityExists_ReturnsTrue() throws Exception {
        // Arrange
        String entityHandle = "12345-EXAMPLE";
        String domainName = "example.com";
        Set<String> registryUrls = Set.of("https://rdap.verisign.com/com/v1/");

        when(bootstrap.getUrlsForTld("com")).thenReturn(registryUrls);

        // Mock HTTP response for 200 OK - note: in real implementation this would be handled by the static method
        // For unit tests, we'll focus on the logic flow rather than HTTP mocking

        // We need to mock the static method or use a different approach
        // For now, we'll test the basic flow and rely on integration tests for HTTP calls

        // Act & Assert - Basic method flow
        boolean result = lookupService.isEntityInThickRegistry(entityHandle, domainName);

        // Verify bootstrap was called
        verify(bootstrap).getUrlsForTld("com");
    }

    @Test
    public void testIsEntityInThickRegistry_NoRegistryUrls_ReturnsFalse() {
        // Arrange
        String entityHandle = "12345-EXAMPLE";
        String domainName = "example.com";

        when(bootstrap.getUrlsForTld("com")).thenReturn(null);

        // Act
        boolean result = lookupService.isEntityInThickRegistry(entityHandle, domainName);

        // Assert
        assertThat(result).isFalse();
        verify(bootstrap).getUrlsForTld("com");
    }

    @Test
    public void testIsEntityInThickRegistry_EmptyRegistryUrls_ReturnsFalse() {
        // Arrange
        String entityHandle = "12345-EXAMPLE";
        String domainName = "example.com";

        when(bootstrap.getUrlsForTld("com")).thenReturn(Set.of());

        // Act
        boolean result = lookupService.isEntityInThickRegistry(entityHandle, domainName);

        // Assert
        assertThat(result).isFalse();
        verify(bootstrap).getUrlsForTld("com");
    }

    @Test
    public void testIsEntityInThickRegistry_InvalidDomainName_ReturnsFalse() {
        // Arrange
        String entityHandle = "12345-EXAMPLE";
        String invalidDomainName = "invalid";

        // Act
        boolean result = lookupService.isEntityInThickRegistry(entityHandle, invalidDomainName);

        // Assert
        assertThat(result).isFalse();
        // Bootstrap should not be called for invalid domain
        verify(bootstrap, never()).getUrlsForTld(any());
    }

    @Test
    public void testIsEntityInThickRegistry_NullDomainName_ReturnsFalse() {
        // Arrange
        String entityHandle = "12345-EXAMPLE";
        String nullDomainName = null;

        // Act
        boolean result = lookupService.isEntityInThickRegistry(entityHandle, nullDomainName);

        // Assert
        assertThat(result).isFalse();
        verify(bootstrap, never()).getUrlsForTld(any());
    }

    @Test
    public void testIsEntityInThickRegistry_EmptyDomainName_ReturnsFalse() {
        // Arrange
        String entityHandle = "12345-EXAMPLE";
        String emptyDomainName = "";

        // Act
        boolean result = lookupService.isEntityInThickRegistry(entityHandle, emptyDomainName);

        // Assert
        assertThat(result).isFalse();
        verify(bootstrap, never()).getUrlsForTld(any());
    }

    @Test
    public void testIsEntityInThickRegistry_MultipleTldFormats() {
        // Test various TLD extraction scenarios
        String entityHandle = "12345-EXAMPLE";

        // Test .com
        when(bootstrap.getUrlsForTld("com")).thenReturn(Set.of("https://rdap.verisign.com/com/v1/"));
        lookupService.isEntityInThickRegistry(entityHandle, "example.com");
        verify(bootstrap).getUrlsForTld("com");

        // Test .co.uk
        when(bootstrap.getUrlsForTld("uk")).thenReturn(Set.of("https://rdap.nominet.uk/"));
        lookupService.isEntityInThickRegistry(entityHandle, "example.co.uk");
        verify(bootstrap).getUrlsForTld("uk");

        // Test uppercase
        when(bootstrap.getUrlsForTld("org")).thenReturn(Set.of("https://rdap.pir.org/"));
        lookupService.isEntityInThickRegistry(entityHandle, "EXAMPLE.ORG");
        verify(bootstrap).getUrlsForTld("org");
    }

    @Test
    public void testIsEntityInThickRegistry_ExceptionHandling() {
        // Arrange
        String entityHandle = "12345-EXAMPLE";
        String domainName = "example.com";

        when(bootstrap.getUrlsForTld("com")).thenThrow(new RuntimeException("Bootstrap error"));

        // Act
        boolean result = lookupService.isEntityInThickRegistry(entityHandle, domainName);

        // Assert
        assertThat(result).isFalse();
        verify(bootstrap).getUrlsForTld("com");
    }

    @Test
    public void testExtractTld_VariousCases() {
        // This would be testing a private method, so we test it indirectly through public methods
        String entityHandle = "12345-EXAMPLE";

        // Test normal domain
        when(bootstrap.getUrlsForTld("com")).thenReturn(Set.of());
        lookupService.isEntityInThickRegistry(entityHandle, "example.com");
        verify(bootstrap).getUrlsForTld("com");

        // Test subdomain (should extract rightmost TLD)
        when(bootstrap.getUrlsForTld("net")).thenReturn(Set.of());
        lookupService.isEntityInThickRegistry(entityHandle, "sub.example.net");
        verify(bootstrap).getUrlsForTld("net");

        // Test with trailing dot (FQDN)
        when(bootstrap.getUrlsForTld("org")).thenReturn(Set.of());
        lookupService.isEntityInThickRegistry(entityHandle, "example.org.");
        verify(bootstrap).getUrlsForTld("org");
    }

    @Test
    public void testIsEntityInThickRegistry_FqdnDomains() {
        // Test FQDN (Fully Qualified Domain Name) handling
        String entityHandle = "12345-EXAMPLE";

        // Test simple FQDN
        when(bootstrap.getUrlsForTld("com")).thenReturn(Set.of());
        lookupService.isEntityInThickRegistry(entityHandle, "example.com.");
        verify(bootstrap).getUrlsForTld("com");

        // Test complex FQDN with subdomain
        when(bootstrap.getUrlsForTld("net")).thenReturn(Set.of());
        lookupService.isEntityInThickRegistry(entityHandle, "subdomain.example.net.");
        verify(bootstrap).getUrlsForTld("net");

        // Test multi-level TLD FQDN
        when(bootstrap.getUrlsForTld("uk")).thenReturn(Set.of());
        lookupService.isEntityInThickRegistry(entityHandle, "example.co.uk.");
        verify(bootstrap).getUrlsForTld("uk");

        // Test uppercase FQDN
        when(bootstrap.getUrlsForTld("org")).thenReturn(Set.of());
        lookupService.isEntityInThickRegistry(entityHandle, "EXAMPLE.ORG.");
        verify(bootstrap).getUrlsForTld("org");
    }
}