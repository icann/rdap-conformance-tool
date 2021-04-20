package org.icann.rdapconformance.validator.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public interface FileSystem {

  void write(String filepath, String data) throws IOException;

  void mkdir(String path) throws IOException;

  String readFile(URI uri) throws IOException;

  boolean exists(String filepath);

  void download(URI uri, String filePath) throws IOException;

  InputStream uriToStream(URI uri) throws IOException;
}
