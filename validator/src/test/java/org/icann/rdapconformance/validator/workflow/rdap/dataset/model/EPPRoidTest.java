package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EPPRoidTest extends BaseUnmarshallingTest<EPPRoid> {

    private EPPRoid eppRoid;

    @BeforeMethod
    public void setup() {
        this.eppRoid = unmarshal("/dataset/epp-repository-ids.xml", EPPRoid.class);
    }

    @Test
    public void givenValidEppRoidXml_whenUnmarshalling_thenReturnEppRoid() {
        assertThat(this.eppRoid.getValues()).hasSize(519);
        assertThat(this.eppRoid.getValues()).contains("GLE",
                "BBC",
                "RSIDE",
                "CHROME",
                "fir",
                "SCOT",
                "AFIN",
                "PFIZER",
                "ÅÄÖ",
                "DVAG",
                "QPON",
                "BRADESCO",
                "CHASE",
                "STAPLES",
                "AMAZONCN",
                "CORSICA",
                "AAA",
                "NEXPRESS",
                "SPGL",
                "AE",
                "SEARCH",
                "POLITIE",
                "GMO",
                "KINDLE",
                "CITADEL",
                "EUROVISN",
                "GUCCI",
                "BCN",
                "CEO",
                "HOT",
                "roid",
                "BEER",
                "BOSTIK",
                "HOW",
                "AR");
    }
}