package org.icann.rdapconformance.tool.progress;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.icann.rdapconformance.validator.CommonUtils;

/**
 * Thread-safe progress tracker for RDAP validation execution with visual progress indicators.
 *
 * <p>This class provides comprehensive progress tracking functionality for RDAP validation
 * operations, managing progress state, coordinating visual updates, and handling different
 * execution phases. It supports both percentage-based progress tracking and pulsing
 * progress bars for long-running operations.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Thread-safe progress tracking with atomic operations</li>
 *   <li>Visual progress bar with pulsing animation for responsive feedback</li>
 *   <li>Phase-based progress tracking for different validation stages</li>
 *   <li>Automatic completion detection and cleanup</li>
 *   <li>Verbose mode support that disables visual progress</li>
 *   <li>Terminal capability detection for appropriate display</li>
 * </ul>
 *
 * <p>The tracker coordinates with ProgressDisplay to provide visual feedback
 * and automatically manages periodic updates for smooth animation. Progress
 * is disabled in verbose mode to avoid interfering with log output.</p>
 *
 * <p>Progress phases help users understand the current stage of validation,
 * from dataset download through various validation stages to completion.
 * The tracker automatically handles transition between phases and cleanup
 * of resources when validation completes.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * ProgressTracker tracker = new ProgressTracker(100, false);
 * tracker.start();
 * tracker.updatePhase(ProgressPhase.VALIDATION);
 * for (int i = 0; i < 100; i++) {
 *     // Perform validation work
 *     tracker.incrementStep();
 * }
 * tracker.complete();
 * </pre>
 *
 * @see ProgressDisplay
 * @see ProgressPhase
 * @since 1.0.0
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

    /**
     * Creates a new progress tracker with the specified configuration.
     *
     * @param totalSteps the total number of steps expected for completion
     * @param verboseMode whether verbose mode is enabled (disables visual progress)
     */
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
     * Starts progress tracking and initializes the visual display.
     *
     * <p>This method initializes the progress display and starts periodic updates
     * for the pulsing animation effect. Progress is only shown when not in verbose
     * mode and when the terminal supports visual progress indicators.</p>
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
     * Updates the current phase of validation execution.
     *
     * <p>This method changes the current phase and updates the display with
     * the new phase name. The progress display is immediately updated to
     * reflect the phase change if visual progress is enabled.</p>
     *
     * @param phase the new progress phase to display
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
     * Updates the current phase with a custom display name.
     *
     * <p>This method allows setting a custom phase name that will be displayed
     * in the progress indicator. This is useful for showing specific operation
     * details beyond the standard phase names.</p>
     *
     * @param phaseName the custom phase name to display
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
     * Increments the progress by one step.
     *
     * <p>This method atomically increments the current progress step and updates
     * the visual display. If the total number of steps is reached, the progress
     * is automatically marked as completed.</p>
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
     * Increments the progress by the specified number of steps.
     *
     * <p>This method atomically adds the specified number of steps to the current
     * progress and updates the visual display. If the total number of steps is
     * reached or exceeded, the progress is automatically marked as completed.</p>
     *
     * @param steps the number of steps to add to the current progress
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
     * Sets the current progress step to a specific value.
     *
     * <p>This method directly sets the progress to the specified step value,
     * clamped between 0 and the total number of steps. The visual display
     * is updated to reflect the new progress value.</p>
     *
     * @param step the step value to set (will be clamped to valid range)
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
     * Marks the progress as completed and cleans up resources.
     *
     * <p>This method sets the progress to completed state, stops periodic updates,
     * and clears the visual progress display. It ensures proper cleanup of the
     * background update thread and finishes the progress indication cleanly.</p>
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
     * Returns the current progress as a percentage (0-100).
     *
     * <p>This method calculates the completion percentage based on the current
     * step count relative to the total number of steps. The returned value
     * is clamped to the range 0-100.</p>
     *
     * @return the current progress percentage (0-100)
     */
    public int getPercentage() {
        int current = currentStep.get();
        if (totalSteps <= CommonUtils.ZERO) {
            return CommonUtils.ZERO;
        }
        return Math.min(MAX_PERCENTAGE, (current * MAX_PERCENTAGE) / totalSteps);
    }

    /**
     * Returns the current step count.
     *
     * @return the current step number
     */
    public int getCurrentStep() {
        return currentStep.get();
    }

    /**
     * Returns the total number of steps configured for this tracker.
     *
     * @return the total number of steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Returns the current progress phase.
     *
     * @return the current ProgressPhase
     */
    public ProgressPhase getCurrentPhase() {
        return currentPhase;
    }

    /**
     * Returns the elapsed time since the tracker was created.
     *
     * @return the elapsed time in milliseconds
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Returns whether the progress tracking has been completed.
     *
     * @return true if progress is completed, false otherwise
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
     * Forces the progress display to be hidden immediately.
     *
     * <p>This method is used in error conditions or when the progress needs
     * to be cleared before the normal completion process. It directly clears
     * the visual display without affecting the internal completion state.</p>
     */
    public void forceHide() {
        if (display.isSupported()) {
            display.clearAndFinish();
        }
    }
}