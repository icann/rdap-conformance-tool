package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import java.net.URI;
import java.util.Set;
import org.icann.rdapconformance.validator.configuration.RDAPValidatorConfiguration;
import org.icann.rdapconformance.validator.workflow.profile.ProfileJsonValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidatorResults;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResponseValidation2Dot4Dot6_2024 extends ProfileJsonValidation {

    private static final Logger logger = LoggerFactory.getLogger(ResponseValidation2Dot4Dot6_2024.class);

    private final RDAPQueryType queryType;
    private final RDAPValidatorConfiguration config;
    private final RDAPDatasetService datasetService;

    public ResponseValidation2Dot4Dot6_2024(String rdapResponse,
        RDAPValidatorResults results,
        RDAPDatasetService datasetService,
        RDAPQueryType queryType,
        RDAPValidatorConfiguration config) {
        super(rdapResponse, results);
        this.datasetService = datasetService;
        this.queryType = queryType;
        this.config = config;
    }


    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_4_6_Validation";
    }

    @Override
    public boolean doValidate() {
        // Validation flow changed per new specs:
        // -47700: validate that registrar entity has a link with rel='about'
        // -47701: validate that 'value' property matches IANA-registered RDAP base URL
        // -47702: validate that 'value' property uses HTTPS scheme
        // -47703: validate that 'href' property is a valid URI format
        // This follows RDAP Response Profile where 'value' contains the registrar's RDAP base URL

        Set<String> registrarEntitiesJsonPointers = getPointerFromJPath(
            "$.entities[?(@.roles contains 'registrar')].links[?(@.rel == 'about')]");

        if (registrarEntitiesJsonPointers.isEmpty()) {
            results.add(RDAPValidationResult.builder()
                .code(-47700)
                .value(getResultValue(getPointerFromJPath("$.entities[?(@.roles contains 'registrar')]")))
                .message(
                    "A domain must have link to the RDAP base URL of the registrar.")
                .build());

            return false;
        }

        String linkPointer = registrarEntitiesJsonPointers.iterator().next();
        String valuePointer = linkPointer + "/value";
        String hrefPointer = linkPointer + "/href";
        ;
        String handlePointer = linkPointer.substring(0, linkPointer.lastIndexOf('/', linkPointer.lastIndexOf('/') - 1)) + "/handle";

        boolean valueValid = true;

        String value = null;
        String href = null;

        Object obj = jsonObject.query(valuePointer);
        if (obj != null) {
            value = obj.toString();
        }
        obj = jsonObject.query(hrefPointer);
        if (obj != null) {
            href = obj.toString();
        }

        String handle = null;
        obj = jsonObject.query(handlePointer);
        if (obj != null) {
            handle = obj.toString();
        }

        // Changed from validating href HTTPS to validating value HTTPS
        // 'value' now contains the RDAP base URL which must be HTTPS
        // we are checking HTTPS first - if it fails, no need to check IANA validation
        if (value == null || !value.startsWith("https")) {
            logger.info("47702, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-47702)
                .value(getResultValue(linkPointer))
                .message(
                    "The registrar RDAP base URL must have an https scheme.")
                .build());

            // no need to validate the url if the protocol is wrong
            return false;
        }

        // Changed from validating value equals request URL to validating value against IANA base URL
        //  "verify that the 'value' property matches one of the base URLs in the registrarId data set"
        if (!validValueRdapUrl(handle, value, linkPointer)) {
            valueValid = false;
        }

        // Changed from validating href against IANA to validating href as valid URI
        // "verify that the href property is a valid URI according to the rules in [webUriValidation]"
        return validHrefUri(href, linkPointer) && valueValid;
    }

    // Changed from validating href to validating value against IANA dataset
    // Now validates that 'value' property matches IANA-registered RDAP base URL
    private boolean validValueRdapUrl(String handle, String value, String linkPointer) {
        int id;

        try {
            id = Integer.parseInt(handle);
        } catch (NullPointerException | NumberFormatException e) {
            // Handle is invalid - this should still be reported as an error instead of being skipped
            logger.info("47701, handle = [{}], is null or not a number", handle);
            results.add(RDAPValidationResult.builder()
                .code(-47701)
                .value(getResultValue(linkPointer))
                .message(
                    "The registrar base URL is not registered with IANA.")
                .build());
            return false;
        }

        RegistrarId registrarId = datasetService.get(RegistrarId.class);
        Record record = registrarId.getById(id);
        String rdapUrl = record.getRdapUrl();

        // Changed validation target from href to value
        if (value == null || !value.equals(rdapUrl)) {
            logger.info("47701, handle/id = {}, rdap url = {}, value = {}", id, rdapUrl, value);
            results.add(RDAPValidationResult.builder()
                .code(-47701)
                .value(getResultValue(linkPointer))
                .message(
                    "The registrar base URL is not registered with IANA.")
                .build());

            return false;
        }

        return true;
    }

    // New method to validate href as valid URI instead of IANA-specific validation
    //  "verify that the href property is a valid URI according to the rules in [webUriValidation]"
    private boolean validHrefUri(String href, String linkPointer) {
        if (href == null) {
            logger.info("47703, href is null");
            results.add(RDAPValidationResult.builder()
                .code(-47703)
                .value(getResultValue(linkPointer))
                .message(
                    "The 'href' property is not a valid Web URI according to [webUriValidation].")
                .build());
            return false;
        }

        // Changed from IANA base URL validation to generic URI validation
        try {
            URI uri = URI.create(href);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Invalid URI format");
            }
        } catch (Exception e) {
            logger.info("47703, href = {}, error = {}", href, e.getMessage());
            results.add(RDAPValidationResult.builder()
                .code(-47703)
                .value(getResultValue(linkPointer))
                .message(
                    "The 'href' property is not a valid Web URI according to [webUriValidation].")
                .build());
            return false;
        }

        return true;
    }


    @Override
    public boolean doLaunch() {
        return queryType.equals(RDAPQueryType.DOMAIN);
    }
}
