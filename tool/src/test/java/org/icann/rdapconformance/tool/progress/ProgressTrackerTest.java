package org.icann.rdapconformance.tool.progress;

import static org.testng.Assert.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit tests for ProgressTracker functionality.
 */
public class ProgressTrackerTest {
    
    private ProgressTracker tracker;
    
    @BeforeMethod
    public void setUp() {
        // Use verbose mode to avoid terminal interactions during testing
        tracker = new ProgressTracker(100, true);
    }
    
    @Test
    public void testInitialState() {
        assertEquals(tracker.getCurrentStep(), 0);
        assertEquals(tracker.getTotalSteps(), 100);
        assertEquals(tracker.getPercentage(), 0);
        assertEquals(tracker.getCurrentPhase(), ProgressPhase.DATASET_DOWNLOAD);
        assertFalse(tracker.isCompleted());
    }
    
    @Test
    public void testIncrementStep() {
        tracker.incrementStep();
        assertEquals(tracker.getCurrentStep(), 1);
        assertEquals(tracker.getPercentage(), 1);
        
        tracker.incrementStep();
        assertEquals(tracker.getCurrentStep(), 2);
        assertEquals(tracker.getPercentage(), 2);
    }
    
    @Test
    public void testIncrementSteps() {
        tracker.incrementSteps(10);
        assertEquals(tracker.getCurrentStep(), 10);
        assertEquals(tracker.getPercentage(), 10);
        
        tracker.incrementSteps(5);
        assertEquals(tracker.getCurrentStep(), 15);
        assertEquals(tracker.getPercentage(), 15);
    }
    
    @Test
    public void testSetCurrentStep() {
        tracker.setCurrentStep(50);
        assertEquals(tracker.getCurrentStep(), 50);
        assertEquals(tracker.getPercentage(), 50);
        
        // Test boundary conditions
        tracker.setCurrentStep(-5);
        assertEquals(tracker.getCurrentStep(), 0);
        
        tracker.setCurrentStep(150);
        assertEquals(tracker.getCurrentStep(), 100);
    }
    
    @Test
    public void testPhaseUpdates() {
        tracker.updatePhase(ProgressPhase.DNS_RESOLUTION);
        assertEquals(tracker.getCurrentPhase(), ProgressPhase.DNS_RESOLUTION);
        
        tracker.updatePhase("CustomPhase");
        // Phase enum might not change, but phase name should be custom
        assertEquals(tracker.getCurrentPhase(), ProgressPhase.DNS_RESOLUTION);
    }
    
    @Test
    public void testAutoCompletion() {
        assertFalse(tracker.isCompleted());
        
        // Setting to total steps should auto-complete
        tracker.setCurrentStep(100);
        assertTrue(tracker.isCompleted());
        assertEquals(tracker.getCurrentPhase(), ProgressPhase.COMPLETED);
    }
    
    @Test
    public void testManualCompletion() {
        tracker.setCurrentStep(50);
        assertFalse(tracker.isCompleted());
        
        tracker.complete();
        assertTrue(tracker.isCompleted());
        assertEquals(tracker.getCurrentStep(), 100);
        assertEquals(tracker.getCurrentPhase(), ProgressPhase.COMPLETED);
    }
    
    @Test
    public void testOperationsAfterCompletion() {
        tracker.complete();
        assertTrue(tracker.isCompleted());
        
        int stepBeforeOperation = tracker.getCurrentStep();
        ProgressPhase phaseBeforeOperation = tracker.getCurrentPhase();
        
        // These operations should be ignored after completion
        tracker.incrementStep();
        tracker.incrementSteps(10);
        tracker.updatePhase(ProgressPhase.DNS_RESOLUTION);
        tracker.updatePhase("SomePhase");
        
        // State should remain unchanged
        assertEquals(tracker.getCurrentStep(), stepBeforeOperation);
        assertEquals(tracker.getCurrentPhase(), phaseBeforeOperation);
    }
    
    @Test
    public void testPercentageCalculation() {
        ProgressTracker smallTracker = new ProgressTracker(10, true);
        
        assertEquals(smallTracker.getPercentage(), 0);
        
        smallTracker.setCurrentStep(1);
        assertEquals(smallTracker.getPercentage(), 10);
        
        smallTracker.setCurrentStep(5);
        assertEquals(smallTracker.getPercentage(), 50);
        
        smallTracker.setCurrentStep(10);
        assertEquals(smallTracker.getPercentage(), 100);
        
        // Test edge case with zero total steps
        ProgressTracker zeroTracker = new ProgressTracker(0, true);
        assertEquals(zeroTracker.getPercentage(), 0);
    }
    
    @Test
    public void testElapsedTime() throws InterruptedException {
        long startTime = System.currentTimeMillis();
        ProgressTracker timeTracker = new ProgressTracker(10, true);
        
        Thread.sleep(100); // Sleep for a bit
        
        long elapsed = timeTracker.getElapsedTime();
        long actualElapsed = System.currentTimeMillis() - startTime;
        
        // Should be roughly the same (within 50ms tolerance)
        assertTrue(Math.abs(elapsed - actualElapsed) < 50, 
                   "Elapsed time should be close to actual elapsed time");
    }
    
    @Test
    public void testIncrementWithZeroOrNegativeSteps() {
        int initialStep = tracker.getCurrentStep();
        
        tracker.incrementSteps(0);
        assertEquals(tracker.getCurrentStep(), initialStep);
        
        tracker.incrementSteps(-5);
        assertEquals(tracker.getCurrentStep(), initialStep);
    }
    
    @Test
    public void testStartMethod() {
        tracker.start();
        // Should not throw exception and should not change completed state
        assertFalse(tracker.isCompleted());
    }
    
    @Test
    public void testForceHide() {
        tracker.start();
        tracker.forceHide();
        // Should not throw exception
        assertFalse(tracker.isCompleted());
    }
    
    @Test
    public void testDoubleComplete() {
        tracker.complete();
        assertTrue(tracker.isCompleted());
        
        // Calling complete again should not cause issues
        tracker.complete();
        assertTrue(tracker.isCompleted());
        assertEquals(tracker.getCurrentPhase(), ProgressPhase.COMPLETED);
    }
    
    @Test
    public void testSetCurrentStepAfterCompletion() {
        tracker.complete();
        assertTrue(tracker.isCompleted());
        
        // This should be ignored
        tracker.setCurrentStep(50);
        assertEquals(tracker.getCurrentStep(), 100);
    }
    
    @Test
    public void testIncrementBeyondTotal() {
        // Set to near completion
        tracker.setCurrentStep(99);
        assertFalse(tracker.isCompleted());
        
        // Increment beyond total should auto-complete
        tracker.incrementStep();
        assertTrue(tracker.isCompleted());
        assertEquals(tracker.getCurrentStep(), 100);
        
        // Further increments should be ignored
        tracker.incrementStep();
        assertEquals(tracker.getCurrentStep(), 100);
    }
    
    @Test
    public void testIncrementStepsBeyondTotal() {
        tracker.setCurrentStep(95);
        
        // Increment by more than remaining should auto-complete
        tracker.incrementSteps(10);
        assertTrue(tracker.isCompleted());
        assertEquals(tracker.getCurrentStep(), 100);
    }
}