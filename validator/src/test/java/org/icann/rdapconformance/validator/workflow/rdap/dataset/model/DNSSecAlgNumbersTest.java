package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class DNSSecAlgNumbersTest extends BaseUnmarshallingTest<DNSSecAlgNumbers> {
    private DNSSecAlgNumbers dnsSecAlgNumbersTest;

    @BeforeMethod
    public void setUp() {
        this.dnsSecAlgNumbersTest = unmarshal("/dataset/dns-sec-alg-numbers.xml", DNSSecAlgNumbers.class);
    }

    @Test
    public void givenValidDnsSecAlgNumbersXml_whenUnmarshalling_thenReturnDnsSecAlgNumbers() throws
                                                                                             NoSuchFieldException,
                                                                                             IllegalAccessException {
        Field field = DNSSecAlgNumbers.class.getDeclaredField("records");
        field.setAccessible(true);

        // Get the value of the private field
        List<DNSSecAlgNumbers.DnsSecAlgNumbersRecord> records = (List<DNSSecAlgNumbers.DnsSecAlgNumbersRecord>) field.get(dnsSecAlgNumbersTest);

        assertThat(records).hasSize(35);
        assertThat(dnsSecAlgNumbersTest.isValid(3)).isTrue();
        assertThat(dnsSecAlgNumbersTest.isValid(253)).isFalse();
        assertThat(records).extracting("number", "signing")
                           .contains(tuple("23", "Y"),
                                   tuple("24-122", ""),
                                   tuple("123-251", ""),
                                   tuple("252", "N"),
                                   tuple("253", "Y"),
                                   tuple("254", "Y"),
                                   tuple("255", ""),
                                   tuple("", ""),
                                   tuple("", ""),
                                   tuple("", ""));
    }


}