package org.icann.rdapconformance.validator.utils;

import emailvalidator4j.ValidationStrategy;
import emailvalidator4j.validator.MXRecord;
import emailvalidator4j.validator.WarningsNotAllowed;
import org.icann.commons.lang.AsciiValidator;
import org.icann.commons.lang.StringUtil;
import org.icann.commons.validation.EmailWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {

    private static final String LABEL_PATTERN = "^[a-zA-Z0-9]+[a-zA-Z0-9-.]*[a-zA-Z0-9]+$|^[a-zA-Z0-9]*[a-zA-Z0-9]*$";
    private static final String LOCAL_PATTERN = "^.{0,}[A-Za-z0-9\\\\!#$%&'*+\\-/=?^_`{|}](?= |$)";

    private final Pattern patternLabel = Pattern.compile(LABEL_PATTERN);
    private final Pattern patternLocal = Pattern.compile(LOCAL_PATTERN);

    private final boolean supportIdn;

    private final emailvalidator4j.EmailValidator delegate;
    private final AsciiValidator asciiValidator;

    public EmailValidator(boolean supportIdn, boolean mxRecordValidation) {
        this.supportIdn = supportIdn;
        List<ValidationStrategy> validationStrategies = new ArrayList<>();
        validationStrategies.add(new WarningsNotAllowed());
        if (mxRecordValidation) {
            validationStrategies.add(new MXRecord());
        }
        delegate = new emailvalidator4j.EmailValidator(validationStrategies);
        asciiValidator = new AsciiValidator();
    }

    public EmailValidator() {
        this(false, false);
    }

    public synchronized boolean validateEmail(String email) {
        if (StringUtil.isBlank(email)) {
            return false;
        } else {
            email = email.trim();
        }

        boolean ascii = asciiValidator.isAscii(email);

        if (!supportIdn && !ascii) {
            return false;
        }

        if (email.endsWith(".")) {
            email = email.substring(0, email.length() - 1);
        }

        EmailWrapper emailWrapper = new EmailWrapper(email);
        String domain = emailWrapper.getDomain();
        String local = emailWrapper.getLocal();

        if (ascii && !validLabelLengthAndFormat(domain)) {
            return false;
        }

        if (StringUtil.isNotBlank(local) && local.length() == 64) {
            boolean valid = validateLocal(local);
            if (!valid) {
                return false;
            }
            local = local.substring(0, local.length() - 1);
            email = local + "@" + domain;
        }

        return !dotlessDomain(email) && delegate.isValid(email);
    }

    private boolean validLabelLengthAndFormat(String domain) {

        if (StringUtil.isBlank(domain) || domain.length() >= 254) {
            return false;
        }

        String[] labels = domain.split("\\.");
        for (String label : labels) {
            Matcher matcher = patternLabel.matcher(label);
            if (label.length() > 63 || !matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    private boolean dotlessDomain(String email) {
        int index = email.indexOf("@");
        return index != -1 && !email.substring(index, email.length() - 1).contains(".");
    }

    private boolean validateLocal(String local) {
        return patternLocal.matcher(local).matches();
    }

}
