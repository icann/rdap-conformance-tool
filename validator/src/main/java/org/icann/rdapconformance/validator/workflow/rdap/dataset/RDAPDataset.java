package org.icann.rdapconformance.validator.workflow.rdap.dataset;

import jakarta.xml.bind.JAXBException;
import org.icann.rdapconformance.validator.workflow.Deserializer;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.JsonDeserializer;
import org.icann.rdapconformance.validator.workflow.XmlDeserializer;
import org.icann.rdapconformance.validator.workflow.rdap.dataset.model.RDAPDatasetModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import static org.icann.rdapconformance.validator.workflow.rdap.RDAPDatasetService.DATASET_PATH;

public abstract class RDAPDataset<T extends RDAPDatasetModel> {

    private static final Logger logger = LoggerFactory.getLogger(RDAPDataset.class);

    private final String name;
    private final URI uri;
    private final FileSystem fileSystem;
    private final String datasetDirectory;
    private final Deserializer<T> deserializer;
    private T modelInstance;

    public RDAPDataset(String name, URI uri, FileSystem fileSystem, Class<T> model) {
        this(name, uri, fileSystem, DATASET_PATH, model);
    }

    public RDAPDataset(String name, URI uri, FileSystem fileSystem, String datasetDirectory, Class<T> model) {
        this.name = name;
        this.fileSystem = fileSystem;
        this.uri = uri;
        this.datasetDirectory = datasetDirectory;
        try {
            this.modelInstance = model.getConstructor().newInstance();
        } catch (Exception e) {
            logger.error("Cannot create an instance of dataset model {}", model.getSimpleName(), e);
            throw new RuntimeException();
        }
        String fileExtension = uri.getPath().substring(uri.getPath().lastIndexOf(".") + 1);
        if ("json".equals(fileExtension)) {
            this.deserializer = new JsonDeserializer<>(model);
        } else if ("xml".equals(fileExtension)) {
            this.deserializer = new XmlDeserializer<>(model);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileExtension);
        }
    }

    private String filename() {
        return uri.toString().substring(uri.toString().lastIndexOf('/') + 1);
    }

    private String filePath() {
        return Paths.get(datasetDirectory, filename()).toAbsolutePath().toString();
    }

    public boolean download(boolean useLocalDatasets) {
        String path = filePath();
        if (useLocalDatasets && this.fileSystem.exists(path)) {
            logger.debug("Dataset {} is already downloaded at: {}", name, path);
            return true;
        }
        logger.debug("Download dataset {}", name);
        try {
            fileSystem.download(uri, path);
            logger.debug("Dataset {} downloaded to {}", name, path);
        } catch (IOException e) {
            logger.error("Failed to download dataset {}", name, e);
            return false;
        }
        return true;
    }

    public boolean parse() {
        String path = filePath();
            try {
                this.modelInstance = deserializer.deserialize(new File(path));
            } catch (JAXBException | IOException e) {
                logger.error("Failed to parse dataset {}", name, e);
                return false;
            }
        return true;
    }

    public String getName() {
        return this.name;
    }

    public T getData() {
        return this.modelInstance;
    }
}
