package org.icann.rdapconformance.validator;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResultsImpl;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.AssertJUnit.assertEquals;

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
}