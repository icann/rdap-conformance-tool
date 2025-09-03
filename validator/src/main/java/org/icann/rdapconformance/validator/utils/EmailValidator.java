package org.icann.rdapconformance.validator.utils;

import org.icann.rdapconformance.validator.utils.lang.AsciiValidator;
import org.icann.rdapconformance.validator.utils.lang.StringUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {

    private static final String LABEL_PATTERN = "^[a-zA-Z0-9]+[a-zA-Z0-9-.]*[a-zA-Z0-9]+$|^[a-zA-Z0-9]*[a-zA-Z0-9]*$";
    private static final String LOCAL_PATTERN = "^.{0,}[A-Za-z0-9\\\\!#$%&'*+\\-/=?^_`{|}](?= |$)";

    private final Pattern patternLabel = Pattern.compile(LABEL_PATTERN);
    private final Pattern patternLocal = Pattern.compile(LOCAL_PATTERN);

    private final AsciiValidator asciiValidator;

    public EmailValidator() {
        asciiValidator = new AsciiValidator();
    }

    public synchronized boolean validateEmail(String email) {
        if (StringUtil.isBlank(email)) {
            return false;
        } else {
            email = email.trim();
        }

        boolean ascii = asciiValidator.isAscii(email);

        if (!ascii) {
            return false;
        }

        // Reject double dot before TLD (e.g. user@domain..com)
        int atIdx = email.indexOf('@');
        if (atIdx > 0) {
            String domain = email.substring(atIdx + 1);
            if (domain.contains("..")) {
                return false;
            }
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

        return !dotlessDomain(email);
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
