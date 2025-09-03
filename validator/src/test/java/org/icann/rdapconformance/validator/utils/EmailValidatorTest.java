package org.icann.rdapconformance.validator.utils;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

public class EmailValidatorTest {
    private final EmailValidator validator = new EmailValidator();

    @Test
    public void testNullAndBlankEmails() {
        assertThat(validator.validateEmail(null)).isFalse();
        assertThat(validator.validateEmail("")).isFalse();
        assertThat(validator.validateEmail("   ")).isFalse();
    }

    @Test
    public void testNonAsciiEmail() {
        assertThat(validator.validateEmail("usér@domain.com")).isFalse();
        assertThat(validator.validateEmail("user@domaín.com")).isFalse();
    }

    @Test
    public void testEmailEndsWithDot() {
        assertThat(validator.validateEmail("user@domain.com.")).isTrue();
        assertThat(validator.validateEmail("user@domain.com.."))
            .isFalse(); // double dot at end is not valid
    }

    @Test
    public void testDotlessDomain() {
        assertThat(validator.validateEmail("user@localhost")).isFalse();
        assertThat(validator.validateEmail("user@com")).isFalse();
    }

    @Test
    public void testValidLabelLengthAndFormat() {
        // label > 63 chars
        String longLabel = "user@" + "a".repeat(64) + ".com";
        assertThat(validator.validateEmail(longLabel)).isFalse();
        // domain > 254 chars
        String longDomain = "user@" + "a".repeat(250) + ".com";
        assertThat(validator.validateEmail(longDomain)).isFalse();
        // valid label
        assertThat(validator.validateEmail("user@domain.com")).isTrue();
    }

    @Test
    public void testLocalPartLengthAndFormat() {
        // local part exactly 64 chars, valid
        String local = "a".repeat(64) + "1";
        String email = local + "@domain.com";
        assertThat(validator.validateEmail(email)).isTrue();
        // local part > 64 chars
        email = "a".repeat(70) + "@" + "domain".repeat(80) + ".com";
        assertThat(validator.validateEmail(email)).isFalse();
    }

    @Test
    public void testInvalidLabelCharacters() {
        assertThat(validator.validateEmail("user@do_main.com")).isFalse();
        assertThat(validator.validateEmail("user@domain-.com")).isFalse();
        assertThat(validator.validateEmail("user@-domain.com")).isFalse();
        assertThat(validator.validateEmail("user@domain..com")).isFalse();
    }

    @Test
    public void testValidEmails() {
        assertThat(validator.validateEmail("user@domain.com")).isTrue();
        assertThat(validator.validateEmail("user.name@sub.domain.co.uk")).isTrue();
        assertThat(validator.validateEmail("user+tag@domain.com")).isTrue();
        assertThat(validator.validateEmail("user_name@domain.com")).isTrue();
        assertThat(validator.validateEmail("user-name@domain.com")).isTrue();
        assertThat(validator.validateEmail("user@domain123.com")).isTrue();
    }

    @Test
    public void testInvalidEmails() {
        assertThat(validator.validateEmail("@domain.com")).isFalse();
        assertThat(validator.validateEmail("user@domain")).isFalse();
        assertThat(validator.validateEmail("user@domain..com")).isFalse();
        assertThat(validator.validateEmail("user@domain.com.."))
            .isFalse();
        assertThat(validator.validateEmail("user@domain.c")).isTrue(); // single char TLD is allowed
        assertThat(validator.validateEmail("user@domain.corporate")).isTrue(); // long TLD
    }

    @Test
    public void testEmailsWithSpecialCharacters() {
        assertThat(validator.validateEmail("user!#$%&'*+-/=?^_`{|}@domain.com")).isTrue();
        assertThat(validator.validateEmail("user@domain.com")).isTrue();
        assertThat(validator.validateEmail("user@domain.com-")).isFalse();
        assertThat(validator.validateEmail("user@domain-.com")).isFalse();
    }

    @Test
    public void testEmailsWithSpaces() {
        assertThat(validator.validateEmail(" user@domain.com ")).isTrue();
        assertThat(validator.validateEmail("user @domain.com")).isTrue();
        assertThat(validator.validateEmail("user@ domain.com")).isFalse();
    }
}

