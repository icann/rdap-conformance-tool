package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import org.testng.annotations.Test;

public class RdapConformanceToolLogLevelTest {

  @Test
  public void testToLogbackLevelKnownLevels() {
    assertThat(RdapConformanceTool.toLogbackLevel("DEBUG")).as("DEBUG").isEqualTo(Level.DEBUG);
    assertThat(RdapConformanceTool.toLogbackLevel("INFO")).as("INFO").isEqualTo(Level.INFO);
    assertThat(RdapConformanceTool.toLogbackLevel("WARN")).as("WARN").isEqualTo(Level.WARN);
    assertThat(RdapConformanceTool.toLogbackLevel("ERROR")).as("ERROR").isEqualTo(Level.ERROR);
  }

  @Test
  public void testToLogbackLevelUnknownFallsBackToError() {
    assertThat(RdapConformanceTool.toLogbackLevel("TRACE")).as("TRACE").isEqualTo(Level.ERROR);
    assertThat(RdapConformanceTool.toLogbackLevel(null)).as("null").isEqualTo(Level.ERROR);
  }
}

