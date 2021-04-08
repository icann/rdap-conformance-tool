package org.icann.rdapconformance.validator.workflow;

import java.io.IOException;

public interface FileSystem {

  void write(String filepath, String data) throws IOException;

  void mkdir(String path) throws IOException;
}
