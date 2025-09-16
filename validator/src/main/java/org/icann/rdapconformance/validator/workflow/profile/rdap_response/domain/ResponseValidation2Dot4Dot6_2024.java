package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

import static org.icann.rdapconformance.validator.CommonUtils.DOT;
import static org.icann.rdapconformance.validator.CommonUtils.HTTP;
import static org.icann.rdapconformance.validator.CommonUtils.HTTPS;
import static org.icann.rdapconformance.validator.CommonUtils.SLASH;

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
    private final RDAPDatasetService datasetService;

    public ResponseValidation2Dot4Dot6_2024(String rdapResponse,
        RDAPValidatorResults results,
        RDAPDatasetService datasetService,
        RDAPQueryType queryType) {
        super(rdapResponse, results);
        this.datasetService = datasetService;
        this.queryType = queryType;
    }


    @Override
    public String getGroupName() {
        return "rdapResponseProfile_2_4_6_Validation";
    }

    @Override
    public boolean doValidate() {
        // Validation flow: Run ALL validations to collect ALL errors
        // -47700: validate that registrar entity has a link with rel='about'
        // -47701: validate that 'value' property matches IANA-registered RDAP base URL
        // -47702: validate that 'value' property uses HTTPS scheme
        // -47703: validate that 'href' property is a valid URI format
        // This follows RDAP Response Profile where 'value' contains the registrar's RDAP base URL

        Set<String> registrarEntitiesJsonPointers = getPointerFromJPath(
            "$.entities[?(@.roles contains 'registrar')].links[?(@.rel == 'about')]");

        // Check -47700: registrar must have a link with rel='about'
        if (registrarEntitiesJsonPointers.isEmpty()) {
            results.add(RDAPValidationResult.builder()
                .code(-47700)
                .value(getResultValue(getPointerFromJPath("$.entities[?(@.roles contains 'registrar')]")))
                .message(
                    "A domain must have link to the RDAP base URL of the registrar.")
                .build());

            // Return false but don't exit early - we want to collect all possible errors
            // However, if there's no link, we can't validate the other properties
            return false;
        }

        String linkPointer = registrarEntitiesJsonPointers.iterator().next();
        String valuePointer = linkPointer + SLASH + "value";
        String hrefPointer = linkPointer + SLASH + "href";
        String handlePointer = linkPointer.substring(0, linkPointer.lastIndexOf('/', linkPointer.lastIndexOf('/') - 1)) + SLASH + "handle";

        // Extract values for validation
        String value = null;
        String href = null;
        String handle = null;

        Object obj = jsonObject.query(valuePointer);
        if (obj != null) {
            value = obj.toString();
        }
        obj = jsonObject.query(hrefPointer);
        if (obj != null) {
            href = obj.toString();
        }
        obj = jsonObject.query(handlePointer);
        if (obj != null) {
            handle = obj.toString();
        }

        // Run ALL validations and collect results - NO early returns
        boolean allValidationsPassed = true;

        // Check -47702: 'value' must use HTTPS scheme
        boolean valueHttpsValid = true;
        if (value == null || !value.startsWith(HTTPS)) {
            logger.info("47702, value = {}", value);
            results.add(RDAPValidationResult.builder()
                .code(-47702)
                .value(getResultValue(linkPointer))
                .message(
                    "The registrar RDAP base URL must have an https scheme.")
                .build());
            valueHttpsValid = false;
            allValidationsPassed = false;
        }

        // Check -47701: 'value' must match IANA-registered RDAP base URL
        // Only validate against IANA if HTTPS check passed (avoid double-reporting URL issues)
        boolean valueIanaValid = true;
        if (valueHttpsValid && !validValueRdapUrl(handle, value, linkPointer)) {
            valueIanaValid = false;
            allValidationsPassed = false;
        }

        // Check -47703: 'href' must be a valid Web URI
        // Always validate href regardless of value validation results
        boolean hrefValid = validHrefUri(href, linkPointer);
        if (!hrefValid) {
            allValidationsPassed = false;
        }

        return allValidationsPassed;
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

    // Validate href as valid Web URI according to the rules in [webUriValidation]
    // This implements the same validation as rdap_common.json "uri" definition:
    // 1. -10401: URI scheme must be 'http' or 'https' 
    // 2. -10400: URI must be syntactically valid according to RFC3986
    // 3. -10402: URI host must pass domain/IP validation
    // All failures result in -47703 for this specific validation context
    // Made public for comprehensive testing
    public boolean validHrefUri(String href, String linkPointer) {
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

        // Web URI validation - Step 1: Check URI syntax (equivalent to -10400)
        URI uri;
        try {
            uri = URI.create(href);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Missing scheme or host component");
            }
        } catch (Exception e) {
            logger.info("47703, href = {}, syntax error = {}", href, e.getMessage());
            results.add(RDAPValidationResult.builder()
                .code(-47703)
                .value(getResultValue(linkPointer))
                .message(
                    "The 'href' property is not a valid Web URI according to [webUriValidation].")
                .build());
            return false;
        }

        // Web URI validation - Step 2: Check scheme is http or https (equivalent to -10401)
        // SECURITY FIX: Handle potential null scheme (should be caught by Step 1, but defense in depth)
        String scheme = uri.getScheme();
        if (scheme == null) {
            logger.info("47703, href = {}, null scheme detected", href);
            results.add(RDAPValidationResult.builder()
                .code(-47703)
                .value(getResultValue(linkPointer))
                .message(
                    "The 'href' property is not a valid Web URI according to [webUriValidation].")
                .build());
            return false;
        }
        
        scheme = scheme.toLowerCase();
        if (!scheme.equals(HTTP) && !scheme.equals(HTTPS)) {
            logger.info("47703, href = {}, invalid scheme = {}", href, scheme);
            results.add(RDAPValidationResult.builder()
                .code(-47703)
                .value(getResultValue(linkPointer))
                .message(
                    "The 'href' property is not a valid Web URI according to [webUriValidation].")
                .build());
            return false;
        }

        // Web URI validation - Step 3: Check host validity (equivalent to -10402)
        String host = uri.getHost();
        if (host == null || host.trim().isEmpty()) {
            logger.info("47703, href = {}, invalid host = {}", href, host);
            results.add(RDAPValidationResult.builder()
                .code(-47703)
                .value(getResultValue(linkPointer))
                .message(
                    "The 'href' property is not a valid Web URI according to [webUriValidation].")
                .build());
            return false;
        }

        // Additional basic host validation - check for invalid characters/format
        // This is a simplified version of the hostname-in-uri validation
        if (host.contains(" ") || host.startsWith(DOT) || host.endsWith(DOT) || host.contains("..")) {
            logger.info("47703, href = {}, malformed host = {}", href, host);
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
