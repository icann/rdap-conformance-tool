package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RegistrarIdDatasetTest {

    private FileSystem mockFileSystem;
    private RegistrarIdDataset dataset;

    @BeforeEach
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new RegistrarIdDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("registrarId");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(RegistrarId.class);
    }

    @Test
    public void testGetName() {
        assertThat(dataset.getName()).isEqualTo("registrarId");
    }
}