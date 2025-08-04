package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SpecialIPv6AddressesDatasetTest {

    private FileSystem mockFileSystem;
    private SpecialIPv6AddressesDataset dataset;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new SpecialIPv6AddressesDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("specialIPv6Addresses");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(SpecialIPv6Addresses.class);
    }

    @Test
    public void testGetName() {
        assertThat(dataset.getName()).isEqualTo("specialIPv6Addresses");
    }
}