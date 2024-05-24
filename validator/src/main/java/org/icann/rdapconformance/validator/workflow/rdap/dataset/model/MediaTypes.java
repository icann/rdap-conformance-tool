package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "registry", namespace = "http://www.iana.org/assignments")
public class MediaTypes implements RDAPDatasetModel, DatasetValidatorModel {

    @XmlElementWrapper(name = "registry", namespace = "http://www.iana.org/assignments")
    @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
    private List<MediaTypesRecord> recordsObject;

    private Set<String> records = new HashSet<>();

    void afterUnmarshal(Unmarshaller u, Object parent) {
        this.records = recordsObject.stream().map(MediaTypesRecord::getName).collect(toSet());
    }

    @Override
    public boolean isInvalid(String subject) {
        return !records.contains(subject);
    }

    public Set<String> getRecords() {
        return records;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    static class MediaTypesRecord {

        @XmlElement(name = "name", namespace = "http://www.iana.org/assignments")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
