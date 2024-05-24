package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistrarId implements RDAPDatasetModel {

    @XmlElementWrapper(name = "registry", namespace = "http://www.iana.org/assignments")
    @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
    private List<Record> records;

    @XmlTransient
    Map<Integer, Record> recordByIdentifier = new HashMap<>();

    Set<String> names = new HashSet<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        this.recordByIdentifier = records.stream().peek(Record::loadRdapUrl)
                                         .collect(Collectors.toMap(Record::getValue, Function.identity()));
        this.names = records.stream().map(Record::getName).collect(Collectors.toSet());
    }

    public boolean containsId(int registrarId) {
        return recordByIdentifier.containsKey(registrarId);
    }

    public Record getById(int registrarId) {
        return recordByIdentifier.get(registrarId);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Record {

        @XmlElement(name = "value", namespace = "http://www.iana.org/assignments")
        private int value;
        @XmlElement(name = "name", namespace = "http://www.iana.org/assignments")
        private String name;
        private String xmlRepresentation;
        @XmlElement(name = "rdapurl", namespace = "http://www.iana.org/assignments")
        private RdapUrl rdapUrlObj = new RdapUrl();
        @XmlTransient
        private String rdapUrl;

        public Record(int value, String name, String rdapUrl, String xmlRepresentation) {
            this.value = value;
            this.name = name;
            this.rdapUrl = rdapUrl;
            this.xmlRepresentation = xmlRepresentation;
        }

        public Record() {
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getRdapUrl() {
            return rdapUrl;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Record record = (Record) o;
            return value == record.value && name.equals(record.name) && Objects
                    .equals(rdapUrl, record.rdapUrl);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, name, rdapUrl);
        }

        @Override
        public String toString() {
            return xmlRepresentation;
        }

        private void loadRdapUrl() {
            if (rdapUrlObj != null) {
                this.rdapUrl = rdapUrlObj.getServer();
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RdapUrl {
        @XmlElement(name = "server", namespace = "http://www.iana.org/assignments")
        private String server = "";

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }
    }
}
