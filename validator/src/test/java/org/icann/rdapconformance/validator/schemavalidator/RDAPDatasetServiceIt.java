package org.icann.rdapconformance.validator.schemavalidator;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RDAPDatasetServiceIt  {
    private RDAPDatasetService rdapDatasetService;
    private FileSystem fileSystem;

    @BeforeMethod
    public void setUp() {
        this.fileSystem = new LocalFileSystem();
        this.rdapDatasetService = new RDAPDatasetService(this.fileSystem);
    }

    @Test
    public void givenValidXml_whenParse_thenAllXmlAreLoaded() {

        rdapDatasetService.download(true);
        MediaTypes mediaTypes = rdapDatasetService.get(MediaTypes.class);
        assertThat(mediaTypes.getRecords()).hasSize(5);
        BootstrapDomainNameSpace bootstrapDomainNameSpace = rdapDatasetService.get(BootstrapDomainNameSpace.class);


    }
}