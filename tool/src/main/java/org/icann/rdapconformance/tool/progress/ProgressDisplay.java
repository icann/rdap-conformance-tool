package org.icann.rdapconformance.tool.progress;

import java.io.Console;

/**
 * Handles the visual display of progress bar in the terminal.
 * Provides curl-style progress indication with current phase and percentage.
 */
public class ProgressDisplay {
    
    private static final int DEFAULT_TERMINAL_WIDTH = 80;
    private static final int RESERVED_SPACE = 35; // Space for text: "[Phase] 100% (9999/9999)"
    private static final int MAX_PHASE_LENGTH = 20;
    
    private final boolean terminalSupported;
    private final int terminalWidth;
    private int lastPercentage = -1;
    
    public ProgressDisplay() {
        this.terminalSupported = isTerminalSupported();
        this.terminalWidth = getTerminalWidth();
    }
    
    /**
     * Update the progress bar display.
     * Only updates if percentage has changed to avoid excessive console writes.
     */
    public void updateProgress(String phase, int current, int total) {
        if (!terminalSupported || total <= 0) {
            return;
        }
        
        int percentage = (current * 100) / total;
        
        // Only update if percentage changed to reduce console writes
        if (percentage == lastPercentage) {
            return;
        }
        
        lastPercentage = percentage;
        
        clearCurrentLine();
        printProgressBar(phase, current, total, percentage);
        System.out.flush();
    }
    
    /**
     * Clear the progress bar and move to next line.
     * Call this when progress is complete or on error.
     */
    public void clearAndFinish() {
        if (terminalSupported) {
            clearCurrentLine();
            System.out.flush();
        }
    }
    
    /**
     * Check if terminal supports ANSI escape codes and is interactive.
     */
    private boolean isTerminalSupported() {
        // Check if we're in an interactive terminal
        Console console = System.console();
        if (console == null) {
            return false; // Not interactive (redirected output)
        }
        
        // Check for common environment variables that indicate terminal support
        String term = System.getenv("TERM");
        if (term == null || term.equals("dumb")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get terminal width, with fallback to default.
     */
    private int getTerminalWidth() {
        try {
            // Try to get terminal width from environment
            String columns = System.getenv("COLUMNS");
            if (columns != null && !columns.isEmpty()) {
                int width = Integer.parseInt(columns);
                if (width > MAX_PHASE_LENGTH) {  // Basic minimum for usable terminal width
                    return width;
                }
            }
        } catch (NumberFormatException e) {
            // Fall through to default
        }
        
        // Platform-specific commands are unreliable, skip external detection
        
        return DEFAULT_TERMINAL_WIDTH;
    }
    
    /**
     * Clear the current line using ANSI escape codes.
     */
    private void clearCurrentLine() {
        System.out.print("\r\033[K");
    }
    
    /**
     * Print the progress bar with specified format.
     */
    private void printProgressBar(String phase, int current, int total, int percentage) {
        // Truncate phase name if too long
        String truncatedPhase = truncatePhase(phase);
        
        // Format the right-side info
        String rightInfo = String.format("%3d%% (%d/%d)", percentage, current, total);
        
        // Build the left part: [PhaseName           ]
        String leftPart = String.format("[%-20s] ", truncatedPhase);
        
        // Calculate exact space available for asterisks
        int availableWidth = terminalWidth - leftPart.length() - rightInfo.length();
        int barWidth = Math.max(1, availableWidth);
        
        // Calculate filled portion
        int filled = (barWidth * current) / total;
        if (filled > barWidth) {
            filled = barWidth;
        }
        
        // Build the asterisk bar to fill exact remaining space
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filled; i++) {
            bar.append("*");
        }
        for (int i = filled; i < barWidth; i++) {
            bar.append(" ");
        }
        
        // Build complete line
        String completeLine = leftPart + bar.toString() + rightInfo;
        
        // Ensure we don't exceed terminal width (safety check)
        if (completeLine.length() > terminalWidth) {
            completeLine = completeLine.substring(0, terminalWidth);
        }
        
        // Print the complete line
        System.out.print(completeLine);
    }
    
    /**
     * Truncate phase name to fit display width.
     */
    private String truncatePhase(String phase) {
        if (phase == null || phase.isEmpty()) {
            return "Unknown";
        }
        
        if (phase.length() <= MAX_PHASE_LENGTH) {
            return phase;
        }
        
        return phase.substring(0, MAX_PHASE_LENGTH - 3) + "...";
    }
    
    /**
     * Check if terminal is supported for external callers.
     */
    public boolean isSupported() {
        return terminalSupported;
    }
}