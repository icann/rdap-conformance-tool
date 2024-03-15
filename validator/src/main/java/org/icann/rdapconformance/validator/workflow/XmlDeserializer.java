package org.icann.rdapconformance.validator.workflow;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;

public class XmlDeserializer<T> implements Deserializer<T> {
    private final Class<T> type;

    public XmlDeserializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public T deserialize(File file) throws IOException, JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(type);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return (T) jaxbUnmarshaller.unmarshal(file);
    }
}
