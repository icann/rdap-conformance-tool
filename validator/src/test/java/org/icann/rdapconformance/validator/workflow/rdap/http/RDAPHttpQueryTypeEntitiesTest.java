package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpQueryTypeProcessor.RDAPHttpQueryType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RDAPHttpQueryTypeEntitiesTest {

    @DataProvider(name = "entitySearchUris")
    public Object[][] entitySearchUris() {
        return new Object[][] {
            {"https://rdap.example/entities?fn=John", "John"},
            {"https://rdap.example/entities?fn=John%20Doe", "John%20Doe"},
            {"https://rdap.example/entities?handle=ABC-123", "ABC-123"},
            {"https://rdap.example/rdap/entities?fn=*", "*"},
        };
    }

    @Test(dataProvider = "entitySearchUris")
    public void entitySearch_isDetectedAsEntities(String uri, String expectedValue) {
        RDAPHttpQueryType type = RDAPHttpQueryType.getType(uri);

        assertThat(type).isEqualTo(RDAPHttpQueryType.ENTITIES);
        assertThat(type.getQueryType()).isEqualTo(RDAPQueryType.ENTITIES);
        assertThat(type.getValue(uri)).isEqualTo(expectedValue);
    }

    @Test
    public void entityLookup_isStillDetectedAsEntity() {
        assertThat(RDAPHttpQueryType.getType("https://rdap.example/entity/ABC-123"))
            .isEqualTo(RDAPHttpQueryType.ENTITY);
    }

    @Test
    public void entitiesWithUnsupportedParameter_isNotDetected() {
        assertThat(RDAPHttpQueryType.getType("https://rdap.example/entities?foo=bar")).isNull();
        assertThat(RDAPHttpQueryType.getType("https://rdap.example/entities")).isNull();
    }

    @Test
    public void entities_isSearchQueryNotLookup() {
        assertThat(RDAPQueryType.ENTITIES.isSearchQuery()).isTrue();
        assertThat(RDAPQueryType.ENTITIES.isLookupQuery()).isFalse();
        assertThat(RDAPQueryType.NAMESERVERS.isSearchQuery()).isTrue();
        assertThat(RDAPQueryType.ENTITY.isSearchQuery()).isFalse();
    }
}
