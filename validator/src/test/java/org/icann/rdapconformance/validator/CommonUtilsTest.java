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
}