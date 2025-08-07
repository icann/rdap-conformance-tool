package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MediaTypesDatasetTest {

    private FileSystem mockFileSystem;
    private MediaTypesDataset dataset;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new MediaTypesDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("mediaTypes");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(MediaTypes.class);
    }

    @Test
    public void testGetName() {
        assertThat(dataset.getName()).isEqualTo("mediaTypes");
    }
}