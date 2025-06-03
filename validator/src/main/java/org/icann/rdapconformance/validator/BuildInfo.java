package org.icann.rdapconformance.validator;

import java.io.InputStream;
import java.util.Properties;

public class BuildInfo {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = BuildInfo.class.getClassLoader().getResourceAsStream("build.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getVersion() {
        return props.getProperty("version", "unknown");
    }

    public static String getBuildDate() {
        return props.getProperty("build.date", "unknown");
    }

}
