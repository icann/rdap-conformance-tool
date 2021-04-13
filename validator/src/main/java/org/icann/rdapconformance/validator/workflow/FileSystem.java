package org.icann.rdapconformance.validator.workflow;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

public interface FileSystem {

  void write(String filepath, String data) throws IOException;

  void mkdir(String path) throws IOException;

  String readFile(String path) throws IOException;

  boolean exists(String filepath);

  void download(URI uri, String filePath) throws IOException;
}
