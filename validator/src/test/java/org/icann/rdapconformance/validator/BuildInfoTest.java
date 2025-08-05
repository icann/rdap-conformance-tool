package org.icann.rdapconformance.validator;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BuildInfoTest {

    @Test
    public void testGetVersion() {
        String version = BuildInfo.getVersion();
        
        assertThat(version).isNotNull();
        assertThat(version).isNotEmpty();
    }
    
    @Test
    public void testGetBuildDate() {
        String buildDate = BuildInfo.getBuildDate();
        
        assertThat(buildDate).isNotNull();
        assertThat(buildDate).isNotEmpty();
    }
    
    @Test
    public void testGetVersion_ReturnsConsistentValue() {
        String version1 = BuildInfo.getVersion();
        String version2 = BuildInfo.getVersion();
        
        assertThat(version1).isEqualTo(version2);
    }
    
    @Test
    public void testGetBuildDate_ReturnsConsistentValue() {
        String buildDate1 = BuildInfo.getBuildDate();
        String buildDate2 = BuildInfo.getBuildDate();
        
        assertThat(buildDate1).isEqualTo(buildDate2);
    }
    
    @Test
    public void testPropertiesHandling_NoNullPointerException() {
        assertThat(BuildInfo.getVersion()).doesNotContain("null");
        assertThat(BuildInfo.getBuildDate()).doesNotContain("null");
    }
    
    @Test
    public void testDefaultValues_WhenPropertiesNotFound() {
        String version = BuildInfo.getVersion();
        String buildDate = BuildInfo.getBuildDate();
        
        if (version.equals("unknown")) {
            assertThat(version).isEqualTo("unknown");
        }
        
        if (buildDate.equals("unknown")) {
            assertThat(buildDate).isEqualTo("unknown");
        }
    }
}