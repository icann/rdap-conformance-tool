package org.icann.rdapconformance.tool.progress;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.icann.rdapconformance.validator.CommonUtils;

/**
 * Thread-safe progress tracker for RDAP validation execution.
 * Manages progress state and coordinates with ProgressDisplay for visual updates.
 */
public class ProgressTracker {
    
    private final int totalSteps;
    private final AtomicInteger currentStep;
    private final ProgressDisplay display;
    private final boolean verboseMode;
    private final long startTime;
    
    private volatile ProgressPhase currentPhase;
    private volatile String currentPhaseName;
    private volatile boolean completed;
    
    // Periodic update mechanism for pulsing progress bar
    private ScheduledExecutorService updateScheduler;
    
    // Constants
    private static final long PULSE_UPDATE_INTERVAL_MS = 500; // 0.5 seconds
    private static final int MAX_PERCENTAGE = 100;
    
    public ProgressTracker(int totalSteps, boolean verboseMode) {
        this.totalSteps = totalSteps;
        this.verboseMode = verboseMode;
        this.currentStep = new AtomicInteger(CommonUtils.ZERO);
        this.display = new ProgressDisplay();
        this.startTime = System.currentTimeMillis();
        this.currentPhase = ProgressPhase.DATASET_DOWNLOAD;
        this.currentPhaseName = currentPhase.getDisplayName();
        this.completed = false;
    }
    
    /**
     * Start progress tracking. Shows initial progress bar and starts periodic updates.
     */
    public void start() {
        if (shouldShowProgress()) {
            updateDisplay();
            startPeriodicUpdates();
        }
    }
    
    /**
     * Start periodic updates for pulsing progress bar effect.
     */
    private void startPeriodicUpdates() {
        updateScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ProgressUpdate");
            t.setDaemon(true);
            return t;
        });
        
        // Update every 0.5 seconds to trigger pulse effect
        updateScheduler.scheduleAtFixedRate(() -> {
            if (!completed && shouldShowProgress()) {
                updateDisplay();
            }
        }, PULSE_UPDATE_INTERVAL_MS, PULSE_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Update the current phase of execution.
     */
    public synchronized void updatePhase(ProgressPhase phase) {
        if (completed) {
            return;
        }
        
        this.currentPhase = phase;
        this.currentPhaseName = phase.getDisplayName();
        
        if (shouldShowProgress()) {
            updateDisplay();
        }
    }
    
    /**
     * Update phase with custom name.
     */
    public synchronized void updatePhase(String phaseName) {
        if (completed) {
            return;
        }
        
        this.currentPhaseName = phaseName;
        
        if (shouldShowProgress()) {
            updateDisplay();
        }
    }
    
    /**
     * Increment progress by one step.
     */
    public void incrementStep() {
        if (completed) {
            return;
        }
        
        int step = currentStep.incrementAndGet();
        
        if (shouldShowProgress()) {
            updateDisplay();
        }
        
        // Auto-complete if we've reached total steps
        if (step >= totalSteps) {
            complete();
        }
    }
    
    /**
     * Increment progress by specified number of steps.
     */
    public void incrementSteps(int steps) {
        if (completed || steps <= CommonUtils.ZERO) {
            return;
        }
        
        int newStep = currentStep.addAndGet(steps);
        
        if (shouldShowProgress()) {
            updateDisplay();
        }
        
        // Auto-complete if we've reached total steps
        if (newStep >= totalSteps) {
            complete();
        }
    }
    
    /**
     * Set current step to specific value.
     */
    public void setCurrentStep(int step) {
        if (completed) {
            return;
        }
        
        currentStep.set(Math.max(CommonUtils.ZERO, Math.min(step, totalSteps)));
        
        if (shouldShowProgress()) {
            updateDisplay();
        }
        
        // Auto-complete if we've reached total steps
        if (step >= totalSteps) {
            complete();
        }
    }
    
    /**
     * Mark progress as completed and clear the progress bar.
     */
    public synchronized void complete() {
        if (completed) {
            return;
        }
        
        completed = true;
        currentPhase = ProgressPhase.COMPLETED;
        currentStep.set(totalSteps);
        
        // Stop periodic updates
        if (updateScheduler != null && !updateScheduler.isShutdown()) {
            updateScheduler.shutdown();
        }
        
        if (shouldShowProgress()) {
            display.clearAndFinish();
        }
    }
    
    /**
     * Get current progress percentage (0-100).
     */
    public int getPercentage() {
        int current = currentStep.get();
        if (totalSteps <= CommonUtils.ZERO) {
            return CommonUtils.ZERO;
        }
        return Math.min(MAX_PERCENTAGE, (current * MAX_PERCENTAGE) / totalSteps);
    }
    
    /**
     * Get current step count.
     */
    public int getCurrentStep() {
        return currentStep.get();
    }
    
    /**
     * Get total steps.
     */
    public int getTotalSteps() {
        return totalSteps;
    }
    
    /**
     * Get current phase.
     */
    public ProgressPhase getCurrentPhase() {
        return currentPhase;
    }
    
    /**
     * Get elapsed time in milliseconds.
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Check if progress is completed.
     */
    public boolean isCompleted() {
        return completed;
    }
    
    /**
     * Determine if progress should be shown based on verbose mode and terminal support.
     */
    private boolean shouldShowProgress() {
        return !verboseMode && !completed && display.isSupported();
    }
    
    /**
     * Update the visual display.
     */
    private void updateDisplay() {
        display.updateProgress(currentPhaseName, currentStep.get(), totalSteps);
    }
    
    /**
     * Force clear progress display (for error conditions).
     */
    public void forceHide() {
        if (display.isSupported()) {
            display.clearAndFinish();
        }
    }
}