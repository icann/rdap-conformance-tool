package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DNSSecAlgNumbers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DNSSecAlgNumbersDatasetTest {

    private FileSystem mockFileSystem;
    private DNSSecAlgNumbersDataset dataset;

    @BeforeMethod
    public void setUp() {
        mockFileSystem = mock(FileSystem.class);
        dataset = new DNSSecAlgNumbersDataset(mockFileSystem);
    }

    @Test
    public void testDatasetCreation() {
        assertThat(dataset.getName()).isEqualTo("dnsSecAlgNumbers");
        assertThat(dataset.getData()).isNotNull();
        assertThat(dataset.getData()).isInstanceOf(DNSSecAlgNumbers.class);
    }

    @Test
    public void testGetName() {
        assertThat(dataset.getName()).isEqualTo("dnsSecAlgNumbers");
    }

    @Test
    public void testGetData() {
        DNSSecAlgNumbers data = dataset.getData();
        assertThat(data).isNotNull();
        assertThat(data).isInstanceOf(DNSSecAlgNumbers.class);
    }
}