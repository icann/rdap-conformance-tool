package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

import java.util.ArrayList;
import java.util.List;

public class ValueAttributeDatasetModel extends EnumDatasetModel<ValueAttributeDatasetModel.ValueAttributeRecord> {

    @XmlElementWrapper(name = "registry", namespace = "http://www.iana.org/assignments")
    @XmlElement(name = "record", namespace = "http://www.iana.org/assignments")
    private List<ValueAttributeRecord> linkRelationRecords = new ArrayList<>();

    @Override
    protected List<ValueAttributeRecord> getValueRecords() {
        return linkRelationRecords;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    protected static class ValueAttributeRecord implements EnumDatasetModelRecord {
        @XmlElement(name = "value", namespace = "http://www.iana.org/assignments")
        private String value;

        public String getValue() {
            return value;
        }
    }

}
