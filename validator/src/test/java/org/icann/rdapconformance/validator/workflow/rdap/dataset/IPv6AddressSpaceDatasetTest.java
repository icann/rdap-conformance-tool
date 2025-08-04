package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class IPv6AddressSpaceDatasetTest {

    private FileSystem mockFileSystem;
    private IPv6AddressSpaceDataset dataset;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new IPv6AddressSpaceDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("ipv6AddressSpace");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(Ipv6AddressSpace.class);
    }

    @Test
    public void testGetName() {
        assertThat(dataset.getName()).isEqualTo("ipv6AddressSpace");
    }
}