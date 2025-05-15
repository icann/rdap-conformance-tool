package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.icann.rdapconformance.validator.CommonUtils.ONE;

public final class ResponseValidation2Dot6Dot3_2024 extends ProfileJsonValidation {
    public ResponseValidation2Dot6Dot3_2024(String rdapResponse, RDAPValidatorResults results) {
        super(rdapResponse, results);
    }

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

        if(statusCodeObject.notice().get("description") instanceof ArrayList<?> noticeDescriptions) {
            if(noticeDescriptions.stream().allMatch(item -> item instanceof String)) {
                List<String> descriptions = (ArrayList<String>) noticeDescriptions;
                var statusDescription = descriptions.stream().filter(desc -> desc.trim().equalsIgnoreCase(
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
        if(statusCodeObject.notice() == null) {
            return false;
        }

        if(statusCodeObject.notice().get("links") instanceof ArrayList<?> noticeLinks) {
            if(noticeLinks.stream().allMatch(item -> item instanceof LinksObjectToValidate)) {
                List<LinksObjectToValidate> links = (ArrayList<LinksObjectToValidate>) noticeLinks;
                var statusLink = links.stream().filter(l -> l.href().trim().equalsIgnoreCase(
                        "https://icann.org/epp")).findAny();
                if(statusLink.isEmpty()) {
                    results.add(RDAPValidationResult.builder()
                            .code(-46604)
                            .value(getResultValue(noticePointersValue))
                            .message("The notice for Status Codes does not have links.")
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

                if(!isValidURL(statusLink.get().value())) {
                    results.add(RDAPValidationResult.builder()
                            .code(-46606)
                            .value(getResultValue(noticePointersValue))
                            .message("The notice for Status Codes does not have a link value of the request URL.")
                            .build());
                    return false;
                }
            }
        } else {
            results.add(RDAPValidationResult.builder()
                    .code(-46603)
                    .value(getResultValue(noticePointersValue))
                    .message("The notice for Status Codes does not have links.")
                    .build());
            return false;
        }

        return true;
    }

    public static boolean isValidURL(String urlString) {
        try {
            URI uri = Paths.get(urlString).toUri();
            uri.toURL();
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}


record StatusCodeObjectToValidate(JSONObject notice, boolean isValid){}
record LinksObjectToValidate(String value, String rel, String href){}
