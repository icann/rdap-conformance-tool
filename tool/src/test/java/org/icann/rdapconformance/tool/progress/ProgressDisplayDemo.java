package org.icann.rdapconformance.tool.progress;

/**
 * Simple demo to test the progress bar display visually.
 * This is not a unit test but a manual testing utility.
 */
public class ProgressDisplayDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Testing Progress Bar Display");
        System.out.println("============================");
        
        // Test basic progress tracking
        ProgressTracker tracker = new ProgressTracker(100, false); // Not verbose, should show progress
        tracker.start();
        
        for (int i = 0; i <= 100; i++) {
            if (i == 20) {
                tracker.updatePhase(ProgressPhase.DATASET_PARSE);
            } else if (i == 40) {
                tracker.updatePhase(ProgressPhase.DNS_RESOLUTION);
            } else if (i == 50) {
                tracker.updatePhase(ProgressPhase.NETWORK_VALIDATION);
            } else if (i == 90) {
                tracker.updatePhase(ProgressPhase.RESULTS_GENERATION);
            }
            
            tracker.setCurrentStep(i);
            Thread.sleep(50); // Slow it down to see the progress
        }
        
        tracker.complete();
        System.out.println("Progress complete!");
        
        // Test with verbose mode (should not show progress bar)
        System.out.println("\nTesting with verbose mode (no progress bar should appear):");
        ProgressTracker verboseTracker = new ProgressTracker(50, true); // Verbose mode
        verboseTracker.start();
        
        for (int i = 0; i <= 50; i++) {
            verboseTracker.incrementStep();
            Thread.sleep(10);
        }
        
        System.out.println("Verbose mode test complete!");
        
        // Test with very long phase name
        System.out.println("\nTesting with long phase names:");
        ProgressTracker longNameTracker = new ProgressTracker(10, false);
        longNameTracker.start();
        
        for (int i = 0; i < 10; i++) {
            longNameTracker.updatePhase("VeryLongPhaseNameThatShouldBeTruncated" + i);
            longNameTracker.incrementStep();
            Thread.sleep(200);
        }
        
        longNameTracker.complete();
        System.out.println("Long name test complete!");
    }
}