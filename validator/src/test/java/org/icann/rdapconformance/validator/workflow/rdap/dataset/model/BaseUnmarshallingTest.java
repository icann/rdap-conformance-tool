package org.icann.rdapconformance.validator.workflow.rdap.dataset.model;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public abstract class BaseUnmarshallingTest<T> {

    protected T unmarshal(String xmlFile, Class<T> modelInstanceClass) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(modelInstanceClass);

            Unmarshaller jaxbUnmarshaller = null;

            jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (T) jaxbUnmarshaller.unmarshal(getClass().getResourceAsStream(xmlFile));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
