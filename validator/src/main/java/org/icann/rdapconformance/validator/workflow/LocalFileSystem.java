package org.icann.rdapconformance.validator.workflow;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LocalFileSystem implements FileSystem {

  @Override
  public void write(String filepath, String data) throws IOException {
    try (FileWriter fileWriter = new FileWriter(filepath)) {
      fileWriter.write(data);
    }
  }

  @Override
  public void mkdir(String path) throws IOException {
    File dir = new File(path);
    if (dir.exists()) {
      if (dir.isDirectory()) {
        return;
      } else if (dir.isFile()) {
        throw new IOException(String
            .format("Cannot create directory %s, a file with the same name already exists", path));
      }
      return;
    }
    if (!dir.mkdir()) {
      throw new IOException("Cannot create directory " + path);
    }
  }
}
