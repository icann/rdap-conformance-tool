package org.icann.rdapconformance.validator.workflow.rdap.http;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;
import org.testng.annotations.Test;

/**
 * Unit tests for  (429 retry backoff logic).
 *
 * <p>{@code getBackoffTime} is private, so these tests invoke it via reflection.</p>
 */
public class RDAPHttpRequestBackoffTest {

    private static long invokeGetBackoffTime(Header[] headers, int attempt) throws Exception {
        Method m = RDAPHttpRequest.class.getDeclaredMethod(
                "getBackoffTime", org.apache.hc.core5.http.Header[].class, int.class);
        m.setAccessible(true);
        return (long) m.invoke(null, headers, attempt);
    }

    private static Header[] retryAfter(String value) {
        return new Header[] { new BasicHeader("Retry-After", value) };
    }

    @Test
    public void retryAfterHeader_isHonored_plusOneSecond() throws Exception {
        // Retry-After: 10  -> 10 + 1 = 11
        long wait = invokeGetBackoffTime(retryAfter("10"), 0);
        assertThat(wait).isEqualTo(11L);
    }

    @Test
    public void retryAfterHeader_isCappedAtMaxRetryTime() throws Exception {
        // Retry-After above cap (120) is capped to 120, then +1 = 121
        long wait = invokeGetBackoffTime(retryAfter("100000"), 0);
        assertThat(wait).isEqualTo(RDAPHttpRequest.MAX_RETRY_TIME + 1L);
    }

    @Test
    public void retryAfterHeader_nonNumeric_fallsBackToExponentialBackoff() throws Exception {
        // Invalid header value -> ignored -> exponential backoff branch used
        long wait = invokeGetBackoffTime(retryAfter("not-a-number"), 0);
        // attempt 0: base * 2^0 = BASE, plus jitter in [0, BASE)
        assertThat(wait).isBetween(
                (long) RDAPHttpRequest.BASE_BACKOFF_SECS,
                (long) (RDAPHttpRequest.BASE_BACKOFF_SECS * 2L));
    }

    @Test
    public void noHeader_usesExponentialBackoff_growsWithAttempt() throws Exception {
        // attempt 0: base * 1  -> [base, 2*base)
        long w0 = invokeGetBackoffTime(null, 0);
        assertThat(w0).isBetween(
                (long) RDAPHttpRequest.BASE_BACKOFF_SECS,
                (long) (RDAPHttpRequest.BASE_BACKOFF_SECS * 2L));

        // attempt 3: base * 2^3 = 8*base  -> [8*base, 8*base + base)
        long w3 = invokeGetBackoffTime(null, 3);
        long expected3 = (long) (RDAPHttpRequest.BASE_BACKOFF_SECS * Math.pow(2, 3));
        assertThat(w3).isBetween(expected3, expected3 + RDAPHttpRequest.BASE_BACKOFF_SECS);
    }

    @Test
    public void noHeader_isCappedAtMaxBackoff() throws Exception {
        // A very large attempt must never exceed MAX_BACKOFF_SECS
        long wait = invokeGetBackoffTime(null, 60);
        assertThat(wait).isLessThanOrEqualTo(RDAPHttpRequest.MAX_BACKOFF_SECS);
    }

    @Test
    public void negativeAttempt_isTreatedAsZero() throws Exception {
        // Math.max(0, attempt) guards against negative exponents
        long wait = invokeGetBackoffTime(null, -5);
        assertThat(wait).isBetween(
                (long) RDAPHttpRequest.BASE_BACKOFF_SECS,
                (long) (RDAPHttpRequest.BASE_BACKOFF_SECS * 2L));
    }
}