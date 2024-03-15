package org.icann.rdapconformance.validator.workflow;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

public interface Deserializer<T> {
    T deserialize(File file) throws IOException, JAXBException;
}
