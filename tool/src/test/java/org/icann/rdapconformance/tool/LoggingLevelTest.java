package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class LoggingLevelTest {

    @Test
    public void allValues_roundTripThroughValueOf() {
        for (LoggingLevel level : LoggingLevel.values()) {
            assertThat(LoggingLevel.valueOf(level.name())).isEqualTo(level);
        }
        assertThat(LoggingLevel.values()).hasSize(5);
    }
}