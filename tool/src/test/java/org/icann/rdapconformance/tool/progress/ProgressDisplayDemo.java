package org.icann.rdapconformance.tool.progress;

/**
 * Demo class to manually test progress bar display in different terminal environments.
 * Run this class to see how the progress bar looks and behaves.
 */
public class ProgressDisplayDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Testing progress bar display...");
        System.out.println("Terminal support: " + new ProgressDisplay().isSupported());
        System.out.println();
        
        ProgressTracker tracker = new ProgressTracker(100, false);
        tracker.start();
        
        // Simulate dataset download phase
        tracker.updatePhase(ProgressPhase.DATASET_DOWNLOAD);
        for (int i = 0; i < 25; i++) {
            tracker.incrementStep();
            Thread.sleep(50);
        }
        
        // Simulate DNS resolution phase
        tracker.updatePhase(ProgressPhase.DNS_RESOLUTION);
        for (int i = 25; i < 35; i++) {
            tracker.incrementStep();
            Thread.sleep(100);
        }
        
        // Simulate network validation phase
        tracker.updatePhase(ProgressPhase.NETWORK_VALIDATION);
        for (int i = 35; i < 90; i++) {
            tracker.incrementStep();
            Thread.sleep(30);
        }
        
        // Simulate results generation phase
        tracker.updatePhase(ProgressPhase.RESULTS_GENERATION);
        for (int i = 90; i < 100; i++) {
            tracker.incrementStep();
            Thread.sleep(100);
        }
        
        tracker.complete();
        System.out.println("Progress test completed!");
    }
}