package org.icann.rdapconformance.tool.progress;

import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProgressTracker functionality.
 */
public class ProgressTrackerTest {

    @Test
    public void testBasicProgressTracking() {
        ProgressTracker tracker = new ProgressTracker(100, true); // verbose mode = true (no display)
        
        assertThat(tracker.getCurrentStep()).isEqualTo(0);
        assertThat(tracker.getTotalSteps()).isEqualTo(100);
        assertThat(tracker.getPercentage()).isEqualTo(0);
        assertThat(tracker.getCurrentPhase()).isEqualTo(ProgressPhase.DATASET_DOWNLOAD);
        assertThat(tracker.isCompleted()).isFalse();
    }
    
    @Test
    public void testIncrementStep() {
        ProgressTracker tracker = new ProgressTracker(10, true);
        
        tracker.incrementStep();
        assertThat(tracker.getCurrentStep()).isEqualTo(1);
        assertThat(tracker.getPercentage()).isEqualTo(10);
        
        tracker.incrementSteps(3);
        assertThat(tracker.getCurrentStep()).isEqualTo(4);
        assertThat(tracker.getPercentage()).isEqualTo(40);
    }
    
    @Test
    public void testSetCurrentStep() {
        ProgressTracker tracker = new ProgressTracker(50, true);
        
        tracker.setCurrentStep(25);
        assertThat(tracker.getCurrentStep()).isEqualTo(25);
        assertThat(tracker.getPercentage()).isEqualTo(50);
        
        // Test bounds
        tracker.setCurrentStep(-5);
        assertThat(tracker.getCurrentStep()).isEqualTo(0);
        
        tracker.setCurrentStep(100);
        assertThat(tracker.getCurrentStep()).isEqualTo(50); // Capped at totalSteps
    }
    
    @Test
    public void testPhaseUpdates() {
        ProgressTracker tracker = new ProgressTracker(100, true);
        
        tracker.updatePhase(ProgressPhase.DNS_RESOLUTION);
        assertThat(tracker.getCurrentPhase()).isEqualTo(ProgressPhase.DNS_RESOLUTION);
        
        tracker.updatePhase("CustomPhase");
        // Phase enum doesn't change with custom name, but display name does
        assertThat(tracker.getCurrentPhase()).isNotNull();
    }
    
    @Test
    public void testAutoCompletion() {
        ProgressTracker tracker = new ProgressTracker(5, true);
        
        // Increment to total steps should auto-complete
        tracker.incrementSteps(5);
        assertThat(tracker.isCompleted()).isTrue();
        assertThat(tracker.getCurrentPhase()).isEqualTo(ProgressPhase.COMPLETED);
        assertThat(tracker.getPercentage()).isEqualTo(100);
    }
    
    @Test
    public void testManualCompletion() {
        ProgressTracker tracker = new ProgressTracker(100, true);
        
        tracker.incrementSteps(50);
        assertThat(tracker.isCompleted()).isFalse();
        
        tracker.complete();
        assertThat(tracker.isCompleted()).isTrue();
        assertThat(tracker.getCurrentPhase()).isEqualTo(ProgressPhase.COMPLETED);
        assertThat(tracker.getCurrentStep()).isEqualTo(100);
        assertThat(tracker.getPercentage()).isEqualTo(100);
    }
    
    @Test
    public void testThreadSafety() throws InterruptedException {
        ProgressTracker tracker = new ProgressTracker(1000, true);
        int numThreads = 10;
        int incrementsPerThread = 100;
        
        Thread[] threads = new Thread[numThreads];
        
        for (int i = 0; i < numThreads; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    tracker.incrementStep();
                    try {
                        Thread.sleep(1); // Small delay to encourage race conditions
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join(5000); // 5 second timeout
        }
        
        // Should have exactly numThreads * incrementsPerThread steps
        assertThat(tracker.getCurrentStep()).isEqualTo(numThreads * incrementsPerThread);
        assertThat(tracker.isCompleted()).isTrue(); // Should auto-complete
    }
    
    @Test
    public void testCompletedOperationsIgnored() {
        ProgressTracker tracker = new ProgressTracker(10, true);
        
        tracker.complete();
        assertThat(tracker.isCompleted()).isTrue();
        
        int stepsBefore = tracker.getCurrentStep();
        
        // These operations should be ignored after completion
        tracker.incrementStep();
        tracker.incrementSteps(5);
        tracker.setCurrentStep(5);
        tracker.updatePhase(ProgressPhase.NETWORK_VALIDATION);
        
        assertThat(tracker.getCurrentStep()).isEqualTo(stepsBefore);
        assertThat(tracker.getCurrentPhase()).isEqualTo(ProgressPhase.COMPLETED);
    }
}