package org.icann.rdapconformance.tool;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.apache.commons.lang3.SystemUtils;
import org.testng.annotations.Test;

public class RdapConformanceToolTest {

  private String getUriStrFromConfig(String fileInput) {
    RdapConformanceTool tool = new RdapConformanceTool();
    tool.configurationFile = fileInput;
    return tool.getConfigurationFile().toString();
  }

  @Test
  public void testLinuxPath() {
    assertThat(getUriStrFromConfig("/tmp/test")).endsWith("/tmp/test");
  }

  @Test
  public void testURIPath() {
    assertThat(getUriStrFromConfig("file:/tmp/test")).endsWith("/tmp/test");
  }

  @Test
  public void testWindowsURIPath() {
    assertThat(getUriStrFromConfig("file:/D:/tmp/test")).endsWith("/tmp/test");
  }

  @Test
  public void testWindowsFilePath() {
    if (SystemUtils.IS_OS_WINDOWS) {
      assertThat(getUriStrFromConfig("D:\\tmp\\test")).endsWith("/tmp/test");
    }
  }
}