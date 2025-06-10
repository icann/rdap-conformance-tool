package org.icann.rdapconformance.validator.workflow.profile.rdap_response.domain;

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

        if (!this.config.getUri().toString().equals(value)) {
            logger.info("47701, value = {}, request url = {}", value, this.config.getUri());
            results.add(RDAPValidationResult.builder()
                .code(-47701)
                .value(getResultValue(linkPointer))
                .message(
                    "The link for registrar RDAP base URL does not have a link value of the request URL.")
                .build());

            valueValid = false;
        }

        if (href == null || !href.startsWith("https")) {
            logger.info("47702, href = {}", href);
            results.add(RDAPValidationResult.builder()
                .code(-47702)
                .value(getResultValue(linkPointer))
                .message(
                    "The registrar RDAP base URL must have an https scheme.")
                .build());

            // no need to validate the url if the protocol is wrong
            return false;
        }

        String handle = null;
        obj = jsonObject.query(handlePointer);
        if (obj != null) {
            handle = obj.toString();
        }

        return validRdapUrl(handle, href, linkPointer) && valueValid;
    }

    private boolean validRdapUrl(String handle, String href, String linkPointer) {
        int id;

        try {
            id = Integer.parseInt(handle);
        } catch (NullPointerException | NumberFormatException e) {
            // this is covered by existing handle validation, so we just skip 47703 test here
            logger.info("handle = [{}], is null or not a number, skip rdap url 47703 validation", handle);

            return true;
        }

        RegistrarId registrarId = datasetService.get(RegistrarId.class);
        Record record = registrarId.getById(id);
        String rdapUrl = record.getRdapUrl();

        if (href == null || !href.equals(rdapUrl)) {
            logger.info("47703, handle/id = {}, rdap url = {}, href = {}", id, rdapUrl, href);
            results.add(RDAPValidationResult.builder()
                .code(-47703)
                .value(getResultValue(linkPointer))
                .message(
                    "The registrar base URL is not registered with IANA.")
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
