package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertNotNull;

public class RDAPJsonValuesTest extends BaseUnmarshallingTest<RDAPJsonValues> {

    private RDAPJsonValues rdapJsonValues;

    @BeforeMethod
    public void setUp() {
        this.rdapJsonValues = unmarshal("/dataset/rdap-json-values.xml", RDAPJsonValues.class);
    }

    @Test
    public void givenRdapJsonValues_whenUnmarshalling_XmlUnmarshalledWithValues() {
        assertNotNull(rdapJsonValues);
        assertThat(rdapJsonValues.getByType(RDAPJsonValues.JsonValueType.ROLE))
                .containsExactly("sponsor",
                        "proxy",
                        "abuse",
                        "noc",
                        "technical",
                        "registrar",
                        "administrative",
                        "reseller",
                        "registrant",
                        "notifications",
                        "billing");
        assertThat(rdapJsonValues.getByType(RDAPJsonValues.JsonValueType.STATUS)).hasSize(36);
        assertThat(rdapJsonValues.getByType(RDAPJsonValues.JsonValueType.REDACTED_EXPRESSION_LANGUAGE)).hasSize(1);
    }

}