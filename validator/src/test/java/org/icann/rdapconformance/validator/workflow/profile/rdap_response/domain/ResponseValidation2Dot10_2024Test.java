package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidationTestBase;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;

public class ResponseValidation2Dot10_2024Test extends ProfileJsonValidationTestBase {

    static final String inaccuracyPointer =
            "#/notices/0:{\"description\":[\"URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"help\",\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\",\"value\":\"https://rdap.cscglobal.com/dbs/rdap-api/v1/domain/cscglobal.com\"}],\"title\":\"test\",\"type\":\"result\"}";
    static final String descriptionPointer =
            "#/notices/0:{\"description\":[\"Calm down and come back later.\"],\"links\":[{\"rel\":\"help\",\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\",\"value\":\"https://rdap.cscglobal.com/dbs/rdap-api/v1/domain/cscglobal.com\"}],\"title\":\"RDDS Inaccuracy Complaint Form\",\"type\":\"result\"}";
    static final String descriptionPointerNoArray =
            "#/notices/0:{\"description\":\"test\",\"links\":[{\"rel\":\"help\",\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\",\"value\":\"https://rdap.cscglobal.com/dbs/rdap-api/v1/domain/cscglobal.com\"}],\"title\":\"RDDS Inaccuracy Complaint Form\",\"type\":\"result\"}";
    static final String linksPointer =
            "#/notices/0:{\"description\":[\"URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf\",\"Calm down and come back later.\"],\"title\":\"RDDS Inaccuracy Complaint Form\",\"type\":\"result\"}";
    static final String hrefPointer =
            "#/notices/0:{\"description\":[\"URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"help\",\"href\":\"test\",\"type\":\"text/html\",\"value\":\"https://rdap.cscglobal.com/dbs/rdap-api/v1/domain/cscglobal.com\"}],\"title\":\"RDDS Inaccuracy Complaint Form\",\"type\":\"result\"}";
    static final String relPointer =
            "#/notices/0:{\"description\":[\"URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"test\",\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\",\"value\":\"https://rdap.cscglobal.com/dbs/rdap-api/v1/domain/cscglobal.com\"}],\"title\":\"RDDS Inaccuracy Complaint Form\",\"type\":\"result\"}";
    static final String valuePointer =
            "#/notices/0:{\"description\":[\"URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf\",\"Calm down and come back later.\"],\"links\":[{\"rel\":\"help\",\"href\":\"https://icann.org/wicf\",\"type\":\"text/html\",\"value\":\"test\"}],\"title\":\"RDDS Inaccuracy Complaint Form\",\"type\":\"result\"}";

    public ResponseValidation2Dot10_2024Test() {
        super("/validators/profile/response_validations/notices_2_Dot_10/valid.json",
                "rdapResponseProfile_2_10_Validation");
    }

    @BeforeMethod
    public void setUp() throws IOException {
        super.setUp();
    }

    @Override
    public ProfileValidation getProfileValidation() {
        return new ResponseValidation2Dot10_2024(
                jsonObject.toString(),
                results);
    }

    @Test
    public void ResponseValidation2Dot10_2024_46701() {
        JSONObject notice = jsonObject.getJSONArray("notices").getJSONObject(0);


        notice.put("title", "test");
        validate(-46701, inaccuracyPointer, "The notice for RDDS Inaccuracy Complaint Form was not found.");
    }

    @Test
    public void ResponseValidation2Dot10_2024_46702() {
        JSONArray descriptions = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("description");

        descriptions.remove(0);
        validate(-46702, descriptionPointer, "The notice for RDDS Inaccuracy Complaint Form does not have the proper description.");
    }

    @Test
    public void ResponseValidation2Dot10_2024_46702_No_Description() {
        JSONObject notice = jsonObject.getJSONArray("notices").getJSONObject(0);

        notice.put("description", "test");
        validate(-46702, descriptionPointerNoArray, "The notice for RDDS Inaccuracy Complaint Form does not have the proper description.");
    }

    @Test
    public void ResponseValidation2Dot10_2024_46703() {
        JSONObject notice = jsonObject.getJSONArray("notices").getJSONObject(0);

        notice.remove("links");
        validate(-46703, linksPointer, "The notice for RDDS Inaccuracy Complaint Form does not have links.");
    }

    @Test
    public void ResponseValidation2Dot10_2024_46704() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("href", "test");
        validate(-46704, hrefPointer, "The notice for RDDS Inaccuracy Complaint Form does not have a link to the complaint form.");
    }

    @Test
    public void ResponseValidation2Dot10_2024_46705() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("rel", "test");
        validate(-46705, relPointer, "The notice for RDDS Inaccuracy Complaint Form does not have a link relation type of help");
    }

    @Test
    public void ResponseValidation2Dot10_2024_46706() {
        JSONObject link = jsonObject.getJSONArray("notices").getJSONObject(0).getJSONArray("links").getJSONObject(0);

        link.put("value", "test");
        validate(-46706, valuePointer, "The notice for RDDS Inaccuracy Complaint Form does not have a link value of the request URL.");
    }
}
