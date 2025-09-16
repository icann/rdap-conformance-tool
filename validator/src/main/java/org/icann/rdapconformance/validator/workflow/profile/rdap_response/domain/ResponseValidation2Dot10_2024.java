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

public class ResponseValidation2Dot10_2024 extends ProfileJsonValidation {

    private final RDAPValidatorConfiguration config;
    private static final String NOT_FOUND = "not_found";

    public ResponseValidation2Dot10_2024(String rdapResponse, RDAPValidatorResults results, RDAPValidatorConfiguration config) {
        super(rdapResponse, results, config);
        this.config = config;
    }

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot10_2024.class);
    private static final String NOTICES_PATH = "$.notices[*]";
    private Set<String> noticePointersValue = null;


    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_10_Validation";
    }

    @Override
    protected boolean doValidate() throws Exception {
        boolean isValid = true;
        noticePointersValue = getPointerFromJPath(NOTICES_PATH);
        var rddsInaccuracy = checkRDDSInaccuracyForNotices();
        isValid = rddsInaccuracy.isValid();
        if(isValid) {
            isValid = isInaccuracyComplaintDescriptionValidate(rddsInaccuracy);
            if(isValid) {
                isValid = hasInaccuracyComplaintLinksArray(rddsInaccuracy);
            }
        }

        return isValid;
    }

    private RDDSInaccuracyObjectToValidate checkRDDSInaccuracyForNotices() {
        JSONObject inaccuracyComplaintNotice = null;
        for (String noticeJsonPointer : noticePointersValue) {
            JSONObject notice = (JSONObject) jsonObject.query(noticeJsonPointer);
            if(notice.get("title") instanceof String noticeTitle) {
                if(noticeTitle.trim().equalsIgnoreCase("RDDS Inaccuracy Complaint Form")) {
                    inaccuracyComplaintNotice = notice;
                }
            }
        }

        if(Objects.isNull(inaccuracyComplaintNotice)) {
            results.add(RDAPValidationResult.builder()
                    .code(-46701)
                    .value(getResultValue(noticePointersValue))
                    .message("The notice for RDDS Inaccuracy Complaint Form was not found.")
                    .build());

            return new RDDSInaccuracyObjectToValidate(inaccuracyComplaintNotice, false);
        }

        return new RDDSInaccuracyObjectToValidate(inaccuracyComplaintNotice, true);
    }

    private boolean isInaccuracyComplaintDescriptionValidate(RDDSInaccuracyObjectToValidate rddsInaccuracyObject) {
        if(rddsInaccuracyObject.notice() == null) {
            return false;
        }

        if(rddsInaccuracyObject.notice().get("description") instanceof JSONArray descriptions) {
            var noticeDescriptions = convertJsonArrayToArrayListOfStrings(descriptions);
            var statusDescription = noticeDescriptions.stream().filter(desc -> desc.trim().equalsIgnoreCase(
                            "URL of the ICANN RDDS Inaccuracy Complaint Form: https://icann.org/wicf"))
                    .findAny();
            if(statusDescription.isEmpty()) {
                results.add(RDAPValidationResult.builder()
                        .code(-46702)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for RDDS Inaccuracy Complaint Form does not have the proper description.")
                        .build());
                return false;
            }
        } else {
            results.add(RDAPValidationResult.builder()
                    .code(-46702)
                    .value(getResultValue(noticePointersValue))
                    .message("The notice for RDDS Inaccuracy Complaint Form does not have the proper description.")
                    .build());
            return false;
        }

        return true;
    }

    private boolean hasInaccuracyComplaintLinksArray(RDDSInaccuracyObjectToValidate rddsInaccuracyObject) {
        Object linksArray;
        if(rddsInaccuracyObject.notice() == null) {
            return false;
        }

        // When links is not found throws an exception
        try {
            linksArray = rddsInaccuracyObject.notice().get("links");
        } catch (JSONException e) {
            results.add(RDAPValidationResult.builder()
                    .code(-46703)
                    .value(getResultValue(noticePointersValue))
                    .message("The notice for RDDS Inaccuracy Complaint Form does not have links.")
                    .build());
            return false;
        }

        if(linksArray instanceof JSONArray links) {
            var noticelinks = convertJsonArrayToArrayListOfObjects(links);
            var inaccuracyComplaintLink = noticelinks.stream().filter(l -> l.href().trim().equalsIgnoreCase(
                    "https://icann.org/wicf")).findAny();
            if(inaccuracyComplaintLink.isEmpty()) {
                results.add(RDAPValidationResult.builder()
                        .code(-46704)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for RDDS Inaccuracy Complaint Form does not have a link to the complaint form.")
                        .build());
                return false;
            }

            if(!inaccuracyComplaintLink.get().rel().equalsIgnoreCase("help")) {
                results.add(RDAPValidationResult.builder()
                        .code(-46705)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for RDDS Inaccuracy Complaint Form does not have a link relation type of help")
                        .build());
                return false;
            }

            if(!inaccuracyComplaintLink.get().value().equalsIgnoreCase(this.config.getUri().toString())) {
                results.add(RDAPValidationResult.builder()
                        .code(-46706)
                        .value(getResultValue(noticePointersValue))
                        .message("The notice for RDDS Inaccuracy Complaint Form does not have a link value of the request URL.")
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


record RDDSInaccuracyObjectToValidate(JSONObject notice, boolean isValid){}
