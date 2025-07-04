package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ResponseValidation2Dot6Dot3_2024 extends ProfileJsonValidation {

    private final RDAPValidatorConfiguration config;
    private static final String NOT_FOUND = "not_found";

    public ResponseValidation2Dot6Dot3_2024(String rdapResponse, RDAPValidatorResults results, RDAPValidatorConfiguration config) {
        super(rdapResponse, results);
        this.config = config;
    }

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot6Dot3_2024.class);
    public static final String NOTICES_PATH = "$.notices[*]";
    private Set<String> noticePointersValue = null;

    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_6_3_Validation";
    }

    @Override
    protected boolean doValidate() throws Exception {
        boolean isValid = true;
        noticePointersValue = getPointerFromJPath(NOTICES_PATH);
        var statusCodes = checkStatusCodeForNotices();
        isValid = statusCodes.isValid();
        if(isValid) {
            isValid = isStatusCodesDescriptionValidate(statusCodes);
             if(isValid) {
                isValid = hasStatusCodesLinksArray(statusCodes);
             }
        }

        return isValid;
    }

    private StatusCodeObjectToValidate checkStatusCodeForNotices() {
        JSONObject statusCodeNotice = null;
        for (String noticeJsonPointer : noticePointersValue) {
            JSONObject notice = (JSONObject) jsonObject.query(noticeJsonPointer);
            if(notice.get("title") instanceof String noticeTitle) {
                if(noticeTitle.trim().equalsIgnoreCase("Status Codes")) {
                    statusCodeNotice = notice;
                }
            }
        }

        if(Objects.isNull(statusCodeNotice)) {
            results.add(RDAPValidationResult.builder()
                    .code(-46601)
                    .value(getResultValue(noticePointersValue))
                    .message("The notice for Status Codes was not found.")
                    .build());

            return new StatusCodeObjectToValidate(statusCodeNotice, false);
        }

        return new StatusCodeObjectToValidate(statusCodeNotice, true);
    }

    private boolean isStatusCodesDescriptionValidate(StatusCodeObjectToValidate statusCodeObject) {
        if(statusCodeObject.notice() == null) {
            return false;
        }

        if(statusCodeObject.notice().get("description") instanceof JSONArray descriptions) {
            var noticeDescriptions = convertJsonArrayToArrayListOfStrings(descriptions);
            var statusDescription = noticeDescriptions.stream().filter(desc -> desc.trim().equalsIgnoreCase(
                    "For more information on domain status codes, please visit https://icann.org/epp"))
                    .findAny();
            if(statusDescription.isEmpty()) {
                results.add(RDAPValidationResult.builder()
                        .code(-46602)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for Status Codes does not have the proper description.")
                        .build());
                return false;
            }
        } else {
            results.add(RDAPValidationResult.builder()
                    .code(-46602)
                    .value(getResultValue(noticePointersValue))
                    .message("The notice for Status Codes does not have the proper description.")
                    .build());
            return false;
        }

        return true;
    }

    private boolean hasStatusCodesLinksArray(StatusCodeObjectToValidate statusCodeObject) {
        Object linksArray;
        if(statusCodeObject.notice() == null) {
            return false;
        }

        // When links is not found throws an exception
        try {
            linksArray = statusCodeObject.notice().get("links");
        } catch (JSONException e) {
            results.add(RDAPValidationResult.builder()
                    .code(-46603)
                    .value(getResultValue(noticePointersValue))
                    .message("The notice for Status Codes does not have links.")
                    .build());
            return false;
        }

        if(linksArray instanceof JSONArray links) {
            var noticelinks = convertJsonArrayToArrayListOfObjects(links);
            var statusLink = noticelinks.stream().filter(l -> l.href().trim().equalsIgnoreCase(
                    "https://icann.org/epp")).findAny();
            if(statusLink.isEmpty()) {
                results.add(RDAPValidationResult.builder()
                        .code(-46604)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for Status Codes does not have a link to the status codes.")
                        .build());
                return false;
            }

            if(!statusLink.get().rel().equalsIgnoreCase("glossary")) {
                results.add(RDAPValidationResult.builder()
                        .code(-46605)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for Status Codes does not have a link relation type of glossary")
                        .build());
                return false;
            }

            if(!statusLink.get().value().equalsIgnoreCase(this.config.getUri().toString())) {
                results.add(RDAPValidationResult.builder()
                        .code(-46606)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for Status Codes does not have a link value of the request URL.")
                        .build());
                return false;
            }
        }

        return true;
    }

    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            logger.info("url is invalid: {}", e.getMessage());
            return false;
        }
    }

    public static List<String> convertJsonArrayToArrayListOfStrings(JSONArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(jsonArray.getString(i));
        }
        return list;
    }

    public static List<LinksObjectToValidate> convertJsonArrayToArrayListOfObjects(JSONArray jsonArray) {
        List<LinksObjectToValidate> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            try {
                list.add(new LinksObjectToValidate(
                        jsonObject.getString("value"),
                        jsonObject.getString("rel"),
                        jsonObject.getString("href")));
            } catch (Exception e) {
                logger.info("Exception trying to convert LinksObjectToValidate {}", e.getMessage());
                list.add(new LinksObjectToValidate(NOT_FOUND, NOT_FOUND, NOT_FOUND));
            }
        }
        return list;
    }
}


record StatusCodeObjectToValidate(JSONObject notice, boolean isValid){}
record LinksObjectToValidate(String value, String rel, String href){}
