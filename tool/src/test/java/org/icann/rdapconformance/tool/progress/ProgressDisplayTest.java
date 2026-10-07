package org.icann.rdapconformance.tool.progress;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ProgressDisplayTest {

    @Test
    public void constructs_withoutThrowing() {
        ProgressDisplay display = new ProgressDisplay();
        assertThat(display).isNotNull();
    }

    @Test
    public void isSupported_returnsBooleanInNonInteractiveEnv() {
        // Under Surefire there is no interactive console, so this is false.
        ProgressDisplay display = new ProgressDisplay();
        assertThat(display.isSupported()).isFalse();
    }

    @Test
    public void updateProgress_noOp_whenTerminalNotSupported() {
        ProgressDisplay display = new ProgressDisplay();
        // Should return early (terminal not supported) without throwing.
        display.updateProgress("DatasetDownload", 1, 10);
        display.updateProgress("DatasetDownload", 1, 10); // same values -> early return branch
        display.updateProgress(null, 0, 0);               // total <= 0 branch
    }

    @Test
    public void updateProgress_handlesZeroAndNegativeTotal() {
        ProgressDisplay display = new ProgressDisplay();
        display.updateProgress("Phase", 5, 0);   // total <= 0
        display.updateProgress("Phase", 5, -1);  // total < 0
    }

    @Test
    public void clearAndFinish_doesNotThrow() {
        ProgressDisplay display = new ProgressDisplay();
        display.clearAndFinish();
    }
}