package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.json.JSONArray;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResponseValidationGtldProfile3NameserverStatusTest extends ProfileJsonValidationTestBase {

    private static final String MESSAGE = "The nameserver object must have a 'status' member.";

    public ResponseValidationGtldProfile3NameserverStatusTest() {
        super("/validators/nameserver/valid.json", "gtldProfile3NameserverStatus_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
        queryContext.setQueryType(RDAPQueryType.NAMESERVER);
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidationGtldProfile3NameserverStatus(queryContext);
    }

    @Test
    public void testValidate_MissingStatus_AddResult66008() {
        jsonObject.remove("status");
        validate(-66008, jsonObject.toString(), MESSAGE);
    }

    @Test
    public void testValidate_EmptyStatusArray_Ok() {
        // Presence is what -66008 checks; values are covered by -49300 and the schema
        jsonObject.put("status", new JSONArray());
        validate();
    }

    @Test
    public void testDoLaunch_OnlyForNameserverQueries() {
        assertThat(getProfileValidation().doLaunch()).isTrue();
        for (RDAPQueryType type : RDAPQueryType.values()) {
            if (type != RDAPQueryType.NAMESERVER) {
                queryContext.setQueryType(type);
                assertThat(getProfileValidation().doLaunch()).as(type.name()).isFalse();
            }
        }
    }

    @Test
    public void testValidate_DomainQueryWithoutStatus_NotLaunched() {
        queryContext.setQueryType(RDAPQueryType.DOMAIN);
        jsonObject.remove("status");
        updateQueryContextJsonData();
        assertThat(getProfileValidation().validate()).isTrue();
    }
}
