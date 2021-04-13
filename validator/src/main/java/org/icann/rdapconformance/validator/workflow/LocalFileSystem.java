package org.icann.rdapconformance.validator.workflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.util.stream.Collectors;

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

  @Override
  public String readFile(String path) throws IOException {
    try (InputStream fis = new FileInputStream(path);
        Reader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr)) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  @Override
  public boolean exists(String filepath) {
    return new File(filepath).exists();
  }

  @Override
  public void download(URI uri, String filePath) throws IOException {
    try (InputStream is = uri.toURL().openStream();
        OutputStream fis = new FileOutputStream(filePath, false)) {
      is.transferTo(fis);
    }
  }
}
