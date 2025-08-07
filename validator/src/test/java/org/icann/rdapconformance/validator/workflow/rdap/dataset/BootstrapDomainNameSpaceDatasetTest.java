package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class BootstrapDomainNameSpaceDatasetTest {

    private FileSystem mockFileSystem;
    private BootstrapDomainNameSpaceDataset dataset;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new BootstrapDomainNameSpaceDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("bootstrapDomainNameSpace");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(BootstrapDomainNameSpace.class);
    }

    @Test
    public void testDatasetUri() {
        // We can't directly access the URI, but we can test the name which indicates the correct setup
        assertThat(dataset.getName()).isEqualTo("bootstrapDomainNameSpace");
    }
}