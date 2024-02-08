package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

public class DsRrTypesTest extends BaseUnmarshallingTest<DsRrTypes> {

    private DsRrTypes dsrRTypes;

    @BeforeMethod
    public void setUp() {
        this.dsrRTypes = unmarshal("/dataset/ds-rr-types.xml", DsRrTypes.class);
    }

    @Test
    public void givenValidXml_whenParse_thenAllXmlAreLoaded() {
        assertThat(dsrRTypes.records).hasSize(8);
        assertThat(dsrRTypes.records).extracting("value", "status").contains(
                tuple("0", "-"),
                tuple("1", "MANDATORY"),
                tuple("2", "MANDATORY"),
                tuple("3", "OPTIONAL"),
                tuple("4", "OPTIONAL"),
                tuple("5", "OPTIONAL"),
                tuple("6", "OPTIONAL"),
                tuple("7-255", "-"));
    }

    @Test
    public void givenValidXml_whenParse_thenValidateTypeAssigned() {
        assertThat(dsrRTypes.isAssigned(0)).isFalse();
        assertThat(dsrRTypes.isAssigned(1)).isTrue();
    }

}