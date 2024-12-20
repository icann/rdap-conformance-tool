package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class MediaTypes implements RDAPDatasetModel, DatasetValidatorModel {

    @XmlElement(name = "registry", namespace = "http://www.iana.org/assignments")
    private List<MediaTypesRegistry> registries;

    private Set<String> records = new HashSet<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        this.records = registries
                .stream()
                .flatMap(MediaTypesRegistry::getRecords)
                .collect(toSet());
    }

    @Override
    public boolean isInvalid(String subject) {
        return !records.contains(subject);
    }

    public Set<String> getRecords() {
        return records;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class MediaTypesRegistry {
        @XmlAttribute(name = "id")
        private String id;

        @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
        private List<MediaTypesRecord> recordsObject = new ArrayList<>();

        public Stream<String> getRecords() {
            return recordsObject.stream()
                    .map(MediaTypesRecord::getTemplate)
                    .filter(Objects::nonNull);
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class MediaTypesRecord {

        @XmlElement(name = "file", namespace = "http://www.iana.org/assignments")
        private String template;

        public String getTemplate() {
            return template;
        }
    }
}
