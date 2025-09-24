package org.icann.rdapconformance.validator;

import java.net.URI;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.Assert.assertThrows;

public class CommonUtilsTest {

    @Test
    public void testConstants() {
        assertEquals(".", CommonUtils.DOT);
        assertEquals("http", CommonUtils.HTTP);
        assertEquals(443, CommonUtils.HTTPS_PORT);
        assertEquals(80, CommonUtils.HTTP_PORT);
    }

    @Test
    public void testAddErrorToResultsFile() {
        try (var mockedStatic = mockStatic(RDAPValidatorResultsImpl.class)) {
            var mockResults = mock(RDAPValidatorResultsImpl.class);
            mockedStatic.when(RDAPValidatorResultsImpl::getInstance).thenReturn(mockResults);

            int code = 404;
            String value = "someValue";
            String message = "Error message";

            CommonUtils.addErrorToResultsFile(code, value, message);

            verify(mockResults, times(1)).add(argThat(result ->
                    result.getCode() == code &&
                            result.getValue().equals(value) &&
                            result.getMessage().equals(message)
            ));
        }
    }

    @Test
    public void testReplaceQueryTypeInStringWith_Domain() {
        String original = "This is a /domain query";
        String replacement = "newDomain";

        String result = CommonUtils.replaceQueryTypeInStringWith(
                RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.DOMAIN, original, replacement);
        assertEquals("This is a newDomain query", result);
    }

    @Test
    public void testReplaceQueryTypeInStringWith_Nameserver() {
        String original = "Resolves the /nameserver info";
        String replacement = "ns1";

        String result = CommonUtils.replaceQueryTypeInStringWith(
                RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVER, original, replacement);
        assertEquals("Resolves the ns1 info", result);
    }

    @Test
    public void testReplaceQueryTypeInStringWith_Autnum() {
        String original = "/autnum range check";
        String replacement = "AS12345";

        String result = CommonUtils.replaceQueryTypeInStringWith(
                RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.AUTNUM, original, replacement);
        assertEquals("AS12345 range check", result);
    }

    @Test
    public void testReplaceQueryTypeInStringWith_Entity() {
        String original = "/entity info lookup";
        String replacement = "entityX";

        String result = CommonUtils.replaceQueryTypeInStringWith(
                RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.ENTITY, original, replacement);
        assertEquals("entityX info lookup", result);
    }

    @Test
    public void testReplaceQueryTypeInStringWith_IP() {
        String original = "/ip address check";
        String replacement = "192.168.1.1";

        String result = CommonUtils.replaceQueryTypeInStringWith(
                RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.IP, original, replacement);
        assertEquals("192.168.1.1 address check", result);
    }

    @Test
    public void testReplaceQueryTypeInStringWith_Nameservers() {
        String original = "Multiple /nameservers";
        String replacement = "ns1, ns2";

        String result = CommonUtils.replaceQueryTypeInStringWith(
                RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.NAMESERVERS, original, replacement);
        assertEquals("Multiple ns1, ns2", result);
    }

    @Test
    public void testReplaceQueryTypeInStringWith_DefaultCase() {
        String original = "/help query";
        String replacement = "assistance";

        String result = CommonUtils.replaceQueryTypeInStringWith(
                RDAPHttpQueryTypeProcessor.RDAPHttpQueryType.HELP, original, replacement);
        assertEquals("/help query", result); // Should remain unchanged for default case
    }

    @Test
    public void testAddErrorToResultsFileWithHttpStatus() {
        try (var mockedStatic = mockStatic(RDAPValidatorResultsImpl.class)) {
            var mockResults = mock(RDAPValidatorResultsImpl.class);
            mockedStatic.when(RDAPValidatorResultsImpl::getInstance).thenReturn(mockResults);

            int httpStatusCode = 404;
            int code = -12345;
            String value = "someValue";
            String message = "Error message";

            CommonUtils.addErrorToResultsFile(httpStatusCode, code, value, message);

            verify(mockResults, times(1)).add(argThat(result ->
                    result.getHttpStatusCode() == httpStatusCode &&
                    result.getCode() == code &&
                    result.getValue().equals(value) &&
                    result.getMessage().equals(message)
            ));
        }
    }

    @Test
    public void testCleanStringFromExtraSlash_NullInput() {
        String result = CommonUtils.cleanStringFromExtraSlash(null);
        assertEquals(null, result);
    }

    @Test
    public void testCleanStringFromExtraSlash_EmptyString() {
        String result = CommonUtils.cleanStringFromExtraSlash("");
        assertEquals("", result);
    }

    @Test
    public void testCleanStringFromExtraSlash_RemovesDoubleSlashes() {
        String input = "https://example.com//path//to//resource";
        String result = CommonUtils.cleanStringFromExtraSlash(input);
        assertEquals("https://example.com//path//to//resource", result);
    }

    @Test
    public void testCleanStringFromExtraSlash_RemovesTrailingSlash() {
        String input = "https://example.com/path/to/resource/";
        String result = CommonUtils.cleanStringFromExtraSlash(input);
        assertEquals("https://example.com/path/to/resource", result);
    }

    @Test
    public void testCleanStringFromExtraSlash_HandlesOnlySlashes() {
        String input = "///";
        String result = CommonUtils.cleanStringFromExtraSlash(input);
        assertEquals("//", result);
    }

    @Test
    public void testCleanStringFromExtraSlash_NoChangesNeeded() {
        String input = "https://example.com/path/to/resource";
        String result = CommonUtils.cleanStringFromExtraSlash(input);
        assertEquals("https://example.com/path/to/resource", result);
    }

    @Test
    public void testCleanStringFromExtraSlash_SingleSlash() {
        String input = "/";
        String result = CommonUtils.cleanStringFromExtraSlash(input);
        assertEquals("", result);
    }

    // ===== Tests for URI validation methods =====

    @Test
    public void testCreateUri_ValidHttps() {
        String href = "https://example.com/path";
        URI result = CommonUtils.createUri(href);
        assertEquals("https", result.getScheme());
        assertEquals("example.com", result.getHost());
        assertEquals("/path", result.getPath());
    }

    @Test
    public void testCreateUri_ValidHttp() {
        String href = "http://test.org:8080/api";
        URI result = CommonUtils.createUri(href);
        assertEquals("http", result.getScheme());
        assertEquals("test.org", result.getHost());
        assertEquals(8080, result.getPort());
        assertEquals("/api", result.getPath());
    }

    @Test
    public void testCreateUri_ThrowsForMissingScheme() {
        String href = "example.com/path";
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.createUri(href);
        });
    }

    @Test
    public void testCreateUri_ThrowsForMissingHost() {
        String href = "https:///path";
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.createUri(href);
        });
    }

    @Test
    public void testCreateUri_ThrowsForMalformedUri() {
        String href = "not a valid uri";
        assertThrows(IllegalArgumentException.class, () -> {
            CommonUtils.createUri(href);
        });
    }

    @Test
    public void testGetUriScheme_ValidUri() {
        URI uri = URI.create("https://example.com");
        String scheme = CommonUtils.getUriScheme(uri);
        assertEquals("https", scheme);
    }

    @Test
    public void testGetUriScheme_HttpUri() {
        URI uri = URI.create("http://test.com");
        String scheme = CommonUtils.getUriScheme(uri);
        assertEquals("http", scheme);
    }

    @Test
    public void testGetUriScheme_NullScheme() {
        // Create a relative URI which has no scheme
        URI uri = URI.create("/path/only");
        String scheme = CommonUtils.getUriScheme(uri);
        assertNull(scheme);
    }

    @Test
    public void testGetUriHost_ValidUri() {
        URI uri = URI.create("https://example.com:443/path");
        String host = CommonUtils.getUriHost(uri);
        assertEquals("example.com", host);
    }

    @Test
    public void testGetUriHost_WithSubdomain() {
        URI uri = URI.create("https://api.example.com/v1");
        String host = CommonUtils.getUriHost(uri);
        assertEquals("api.example.com", host);
    }

    @Test
    public void testGetUriHost_NullHost() {
        // Create a URI without host (like a relative path)
        URI uri = URI.create("/path/only");
        String host = CommonUtils.getUriHost(uri);
        assertNull(host);
    }

    @Test
    public void testCreateUri_EdgeCaseIpv6() {
        String href = "https://[::1]:8080/path";
        URI result = CommonUtils.createUri(href);
        assertEquals("https", result.getScheme());
        assertEquals("[::1]", result.getHost());
        assertEquals(8080, result.getPort());
    }

    @Test
    public void testCreateUri_WithQuery() {
        String href = "https://example.com/search?q=test&limit=10";
        URI result = CommonUtils.createUri(href);
        assertEquals("https", result.getScheme());
        assertEquals("example.com", result.getHost());
        assertEquals("/search", result.getPath());
        assertEquals("q=test&limit=10", result.getQuery());
    }

    @Test
    public void testCreateUri_WithFragment() {
        String href = "https://example.com/page#section1";
        URI result = CommonUtils.createUri(href);
        assertEquals("https", result.getScheme());
        assertEquals("example.com", result.getHost());
        assertEquals("/page", result.getPath());
        assertEquals("section1", result.getFragment());
    }
}