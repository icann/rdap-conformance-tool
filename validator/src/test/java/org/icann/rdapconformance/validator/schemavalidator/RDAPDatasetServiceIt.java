package org.icann.rdapconformance.validator.schemavalidator;

import static org.assertj.core.api.Assertions.assertThat;

import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetServiceImpl;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.DNSSecAlgNumbersDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.DsRrTypesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.EPPRoidDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.IPv4AddressSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.IPv6AddressSpaceDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.LinkRelationsDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.RDAPExtensionsDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.RDAPJsonValuesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.RegistrarIdDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.SpecialIPv4AddressesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.SpecialIPv6AddressesDataset;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.BootstrapDomainNameSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DNSSecAlgNumbers;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.DsRrTypes;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.EPPRoid;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv4AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.Ipv6AddressSpace;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.LinkRelations;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.MediaTypes;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPExtensions;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPJsonValues.JsonValueType;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RegistrarId;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv4Addresses;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.SpecialIPv6Addresses;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RDAPDatasetServiceIt {

    private RDAPDatasetService rdapDatasetService;
    private FileSystem fileSystem;

    @BeforeMethod
    public void setUp() {
        this.fileSystem = new LocalFileSystem();
        this.rdapDatasetService = new RDAPDatasetServiceImpl(this.fileSystem);
    }

    @Test
    public void givenValidXml_whenParse_thenAllXmlAreLoaded() {
        rdapDatasetService.download(true);
        MediaTypes mediaTypes = rdapDatasetService.get(MediaTypes.class);
        assertThat(mediaTypes.getRecords()).isNotEmpty();
        assertThat(mediaTypes.isInvalid("invalid")).isTrue();
        assertThat(mediaTypes.isInvalid("raw")).isFalse();

        BootstrapDomainNameSpace bootstrapDomainNameSpace = rdapDatasetService.get(BootstrapDomainNameSpace.class);
        assertThat(bootstrapDomainNameSpace.getTlds()).isNotEmpty();
        assertThat(bootstrapDomainNameSpace.tldExists("com")).isTrue();
        assertThat(bootstrapDomainNameSpace.tldExists("test")).isFalse();

        Ipv4AddressSpace ipv4AddressSpace = rdapDatasetService.get(Ipv4AddressSpace.class);
        assertThat(ipv4AddressSpace.isInvalid("127.0.0.1")).isTrue();
        assertThat(ipv4AddressSpace.isInvalid("1.0.0.0/8")).isFalse();

        SpecialIPv4Addresses specialIPv4Addresses = rdapDatasetService.get(SpecialIPv4Addresses.class);
        assertThat(specialIPv4Addresses.getValues()).isNotEmpty();
        assertThat(specialIPv4Addresses.isInvalid("127.0.0.1")).isTrue();
        assertThat(specialIPv4Addresses.isInvalid("1.0.0.0/8")).isFalse();

        Ipv6AddressSpace iPv6AddressSpace = rdapDatasetService.get(Ipv6AddressSpace.class);
        assertThat(iPv6AddressSpace.isInvalid("any")).isFalse();

        SpecialIPv6Addresses specialIPv6Addresses = rdapDatasetService.get(SpecialIPv6Addresses.class);
        assertThat(specialIPv6Addresses.getValues()).isNotEmpty();
        assertThat(specialIPv6Addresses.isInvalid("any")).isFalse();

        RDAPExtensions rdapExtensions = rdapDatasetService.get(RDAPExtensions.class);
        assertThat(rdapExtensions.getValues()).isNotEmpty();
        assertThat(rdapExtensions.isInvalid("invalid")).isTrue();
        assertThat(rdapExtensions.isInvalid("cidr0")).isFalse();

        LinkRelations linkRelations = rdapDatasetService.get(LinkRelations.class);
        assertThat(linkRelations.getValues()).isNotEmpty();
        assertThat(linkRelations.isInvalid("invalid")).isTrue();
        assertThat(linkRelations.isInvalid("alternate")).isFalse();

        RDAPJsonValues rdapJsonValues = rdapDatasetService.get(RDAPJsonValues.class);
        assertThat(rdapJsonValues.getByType(JsonValueType.STATUS)).isNotEmpty();

        DsRrTypes dsRrTypes = rdapDatasetService.get(DsRrTypes.class);
        assertThat(dsRrTypes.isAssigned(1)).isTrue();
        assertThat(dsRrTypes.isAssigned(0)).isFalse();

        DNSSecAlgNumbers dnsSecAlgNumbers = rdapDatasetService.get(DNSSecAlgNumbers.class);
        assertThat(dnsSecAlgNumbers.isValid(8)).isTrue();
        assertThat(dnsSecAlgNumbers.isValid(0)).isFalse();

        RegistrarId registrarId = rdapDatasetService.get(RegistrarId.class);
        assertThat(registrarId.containsId(0)).isFalse();
        assertThat(registrarId.containsId(14)).isTrue();

        EPPRoid eppRoid = rdapDatasetService.get(EPPRoid.class);
        assertThat(eppRoid.getValues()).isNotEmpty();
        assertThat(eppRoid.isInvalid("invalid")).isTrue();
        assertThat(eppRoid.isInvalid("VRSN")).isFalse();
    }
}