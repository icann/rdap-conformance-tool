package org.icann.rdapconformance.validator.workflow.profile.rdap_response.general;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.icann.rdapconformance.validator.QueryContext;
import org.icann.rdapconformance.validator.workflow.profile.ProfileValidation;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * gTLD Profile v3 conformance: the RDAP response must contain exactly one
 * Access-Control-Allow-Origin HTTP response header. Emits -66003 when the
 * header is missing or appears more than once.
 *
 * <p>Complements TigValidation1Dot13 (-20500), which checks the header value
 * is "*"; this validation checks the header cardinality.</p>
 */
public final class ResponseValidationGtldProfile3CorsHeader extends ProfileValidation {

    private static final Logger logger =
        LoggerFactory.getLogger(ResponseValidationGtldProfile3CorsHeader.class);

    static final String ACAO_HEADER = "Access-Control-Allow-Origin";

    private final HttpResponse<String> rdapResponse;
    private final QueryContext queryContext;

    public ResponseValidationGtldProfile3CorsHeader(QueryContext queryContext) {
        super(queryContext.getResults());
        this.rdapResponse = queryContext.getCurrentHttpResponse();
        this.queryContext = queryContext;
    }

    @Override
    public String getGroupName() {
        return "gtldProfile3CorsHeader_Validation";
    }

    @Override
    protected boolean doValidate() {
        if (rdapResponse == null) {
            logger.debug("No HTTP response available, skipping -66003 check.");
            return true;
        }

        boolean isValid = true;
        Optional<HttpResponse<String>> responseOpt = Optional.of(rdapResponse);
        while (responseOpt.isPresent()) {
            HttpResponse<String> response = responseOpt.get();
            // allValues() is case-insensitive and returns one entry per header line
            List<String> acaoValues = response.headers().allValues(ACAO_HEADER);

            if (acaoValues.size() != 1) {
                results.add(RDAPValidationResult.builder()
                    .code(-66003)
                    .value(formatHeaders(response))
                    .message("The RDAP response must contain exactly one "
                        + "Access-Control-Allow-Origin HTTP response header.")
                    .build(queryContext));
                isValid = false;
            }
            responseOpt = response.previousResponse();
        }
        return isValid;
    }

    private static String formatHeaders(HttpResponse<String> response) {
        return response.headers().map().entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue().toString())
            .collect(Collectors.joining(", "));
    }
}
