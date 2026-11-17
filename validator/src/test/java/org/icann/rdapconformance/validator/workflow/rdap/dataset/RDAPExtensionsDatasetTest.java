package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class RDAPExtensionsDatasetTest {

    private FileSystem mockFileSystem;
    private RDAPExtensionsDataset dataset;

    @BeforeEach
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new RDAPExtensionsDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("RDAPExtensions");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(RDAPExtensions.class);
    }

    @Test
    public void testGetName() {
        assertThat(dataset.getName()).isEqualTo("RDAPExtensions");
    }
}