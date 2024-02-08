package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.icann.rdapconformance.validator.workflow.JsonDeserializer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class BootstrapDomainNameSpaceTest extends BaseUnmarshallingTest<BootstrapDomainNameSpace> {

    private BootstrapDomainNameSpace bootstrapDomainNameSpace;// = new BootstrapDomainNameSpace();
    private JsonDeserializer<BootstrapDomainNameSpace> deserializer = new JsonDeserializer<>(BootstrapDomainNameSpace.class);

    @BeforeMethod
    public void setUp() throws Throwable {
        //this.bootstrapDomainNameSpace.parse(getClass().getResourceAsStream("/dataset/dns.json"));;
        this.bootstrapDomainNameSpace = deserializer.deserialize(new File(getClass().getResource("/dataset/dns.json").getFile()));
    }

    @Test
    public void givenValidDnsJsonFile_whenDeserialize_thenAllTldsAreLoaded() {
        assertThat(bootstrapDomainNameSpace.getTlds()).hasSize(1165);
        assertThat(bootstrapDomainNameSpace.getUrlsForTld("com")).contains("https://rdap.verisign.com/com/v1/");
        assertThat(bootstrapDomainNameSpace.getUrlsForTld("ally")).contains("https://rdap.afilias-srs.net/rdap/ally/");

        System.out.println(bootstrapDomainNameSpace.getUrlsForTld("ryukyu"));
        System.out.println(bootstrapDomainNameSpace.getUrlsForTld("sharp"));
    }
}