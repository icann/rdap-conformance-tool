package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LinkRelationsDatasetTest {

    private FileSystem mockFileSystem;
    private LinkRelationsDataset dataset;

    @BeforeEach
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new LinkRelationsDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("linkRelations");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(LinkRelations.class);
    }

    @Test
    public void testGetName() {
        assertThat(dataset.getName()).isEqualTo("linkRelations");
    }
}