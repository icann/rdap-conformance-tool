package org.icann.rdapconformance.tool.progress;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class ProgressPhaseTest {

    @Test
    public void allPhases_exposeDisplayName() {
        for (ProgressPhase phase : ProgressPhase.values()) {
            assertThat(phase.getDisplayName()).isNotBlank();
            assertThat(phase.toString()).isEqualTo(phase.getDisplayName());
        }
    }

    @Test
    public void valueOf_roundTrips() {
        assertThat(ProgressPhase.valueOf("COMPLETED")).isEqualTo(ProgressPhase.COMPLETED);
        assertThat(ProgressPhase.DATASET_DOWNLOAD.getDisplayName()).isEqualTo("DatasetDownload");
    }
}