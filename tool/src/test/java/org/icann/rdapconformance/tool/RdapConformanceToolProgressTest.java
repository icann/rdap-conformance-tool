package org.icann.rdapconformance.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.icann.rdapconformance.tool.progress.ProgressPhase;
import org.icann.rdapconformance.tool.progress.ProgressTracker;
import org.testng.annotations.Test;

/**
 * Covers the progress helper methods, step calculation, the
 * DatasetProgressCallback inner class and isValidIpAddress — all without
 * network access, using reflection where members are private.
 */
public class RdapConformanceToolProgressTest {

    // ---------- helpers ----------

    private static void setTracker(RdapConformanceTool tool, ProgressTracker tracker) throws Exception {
        Field f = RdapConformanceTool.class.getDeclaredField("progressTracker");
        f.setAccessible(true);
        f.set(tool, tracker);
    }

    private static Object newCallback(RdapConformanceTool tool) throws Exception {
        Class<?> cbClass = Class.forName(
                "org.icann.rdapconformance.tool.RdapConformanceTool$DatasetProgressCallback");
        Constructor<?> ctor = cbClass.getDeclaredConstructor(RdapConformanceTool.class);
        ctor.setAccessible(true);
        return ctor.newInstance(tool);
    }

    private static void invokeCallback(Object cb, String method, Object... args) throws Exception {
        for (Method m : cb.getClass().getDeclaredMethods()) {
            if (m.getName().equals(method)) {
                m.setAccessible(true);
                m.invoke(cb, args);
                return;
            }
        }
        throw new NoSuchMethodException(method);
    }

    // ---------- progress helpers: null tracker branch ----------

    @Test
    public void progressHelpers_nullTracker_areNoOps() {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.updateProgressPhase(ProgressPhase.DNS_RESOLUTION);
        tool.updateProgressPhase("CustomPhase");
        tool.incrementProgress();
        tool.incrementProgress(5);
        tool.completeProgress();
        assertThat(tool.getProgressTracker()).isNull();
    }

    // ---------- progress helpers: non-null tracker branch ----------

    @Test
    public void progressHelpers_withTracker_delegate() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        ProgressTracker tracker = new ProgressTracker(100, true);
        setTracker(tool, tracker);

        tool.updateProgressPhase(ProgressPhase.NETWORK_VALIDATION);
        tool.updateProgressPhase("IPv4-JSON");
        tool.incrementProgress();
        tool.incrementProgress(5);
        assertThat(tracker.getCurrentStep()).isEqualTo(6);

        tool.completeProgress();
        assertThat(tracker.isCompleted()).isTrue();
        assertThat(tool.getProgressTracker()).isSameAs(tracker);
    }

    // ---------- step calculation branches ----------

    @Test
    public void calculateTotalSteps_bothProtocols() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        Method m = RdapConformanceTool.class.getDeclaredMethod("calculateTotalSteps");
        m.setAccessible(true);
        int both = (int) m.invoke(tool);

        tool.setExecuteIPv4Queries(false); // auto-enables IPv6 only
        int v6only = (int) m.invoke(tool);

        assertThat(both).isGreaterThan(v6only); // 4 rounds vs 2 rounds
    }

    @Test
    public void getNetworkValidationSteps_perProtocol() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        Method m = RdapConformanceTool.class.getDeclaredMethod("getNetworkValidationSteps");
        m.setAccessible(true);

        int both = (int) m.invoke(tool);
        tool.setExecuteIPv6Queries(false); // IPv4 only
        int v4only = (int) m.invoke(tool);

        assertThat(both).isEqualTo(v4only * 2);
    }

    // ---------- initializeProgressTracking branches ----------

    @Test
    public void initializeProgressTracking_skippedWhenVerbose() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setVerbose(true);
        Method m = RdapConformanceTool.class.getDeclaredMethod("initializeProgressTracking");
        m.setAccessible(true);
        m.invoke(tool);
        assertThat(tool.getProgressTracker()).isNull();
    }

    @Test
    public void initializeProgressTracking_createsTrackerInCliMode() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        tool.setVerbose(false);
        tool.setShowProgress(true);
        Method m = RdapConformanceTool.class.getDeclaredMethod("initializeProgressTracking");
        m.setAccessible(true);
        m.invoke(tool);
        assertThat(tool.getProgressTracker()).isNotNull();
        tool.completeProgress(); // cleanup scheduler
    }

    // ---------- DatasetProgressCallback ----------

    @Test
    public void datasetCallback_nullTracker_allMethodsSafe() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        Object cb = newCallback(tool);

        invokeCallback(cb, "onDatasetDownloadStarted", "ds");
        invokeCallback(cb, "onDatasetDownloadCompleted", "ds");
        invokeCallback(cb, "onDatasetParseStarted", "ds");
        invokeCallback(cb, "onDatasetParseCompleted", "ds");
        invokeCallback(cb, "onDatasetError", "ds", "download", new RuntimeException("x"));
    }

    @Test
    public void datasetCallback_withTracker_countsOperations() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        ProgressTracker tracker = new ProgressTracker(100, true);
        setTracker(tool, tracker);
        Object cb = newCallback(tool);

        invokeCallback(cb, "onDatasetDownloadCompleted", "ds1");
        invokeCallback(cb, "onDatasetParseCompleted", "ds1");
        invokeCallback(cb, "onDatasetError", "ds2", "parse", new RuntimeException("x"));
        assertThat(tracker.getCurrentStep()).isEqualTo(3);
    }

    @Test
    public void datasetCallback_downloadStarted_localVsRemote() throws Exception {
        // local datasets branch
        RdapConformanceTool local = new RdapConformanceTool();
        local.setUseLocalDatasets(true);
        invokeCallback(newCallback(local), "onDatasetDownloadStarted", "ds");

        // remote branch
        RdapConformanceTool remote = new RdapConformanceTool();
        remote.setUseLocalDatasets(false);
        invokeCallback(newCallback(remote), "onDatasetDownloadStarted", "ds");
    }

    // ---------- isValidIpAddress ----------

    @Test
    public void isValidIpAddress_allBranches() throws Exception {
        RdapConformanceTool tool = new RdapConformanceTool();
        Method m = RdapConformanceTool.class.getDeclaredMethod("isValidIpAddress", String.class);
        m.setAccessible(true);

        assertThat((boolean) m.invoke(tool, (Object) null)).isFalse();
        assertThat((boolean) m.invoke(tool, "")).isFalse();
        assertThat((boolean) m.invoke(tool, "   ")).isFalse();
        assertThat((boolean) m.invoke(tool, "8.8.8.8")).isTrue();
        assertThat((boolean) m.invoke(tool, "2001:4860:4860::8888")).isTrue();
        assertThat((boolean) m.invoke(tool, "not-an-ip")).isFalse();
        assertThat((boolean) m.invoke(tool, "999.999.999.999")).isFalse();
    }
}