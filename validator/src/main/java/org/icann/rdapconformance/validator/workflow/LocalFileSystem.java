package org.icann.rdapconformance.validator.workflow;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
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
  public String readFile(URI uri) throws IOException {
    try (InputStream fis = new FileInputStream(uri.getPath());
        Reader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr)) {
      return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }

  @Override
  public boolean exists(String filepath) {
    if (filepath == null || filepath.trim().isEmpty()) {
      return false;
    }
    return new File(filepath).exists();
  }

  @Override
  public void download(URI uri, String filePath) throws IOException {
    try (InputStream is = uri.toURL().openStream();
        OutputStream fis = new FileOutputStream(filePath, false)) {
      is.transferTo(fis);
    }
  }

  @Override
  public InputStream uriToStream(URI uri) throws IOException {
    if (!uri.isAbsolute()) {
      String filePath = Path.of(uri.toString()).toAbsolutePath().toString();
      uri = new File(filePath).toURI();
    }
    if (null == uri.getScheme()) {
      uri = URI.create("file://" + uri);
    }
    return uri.toURL().openStream();
  }
}
