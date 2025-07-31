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
                if (width > RESERVED_SPACE) {
                    return width;
                }
            }
        } catch (NumberFormatException e) {
            // Fall through to default
        }
        
        // Try using stty command (Unix/Linux/macOS)
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "stty size 2>/dev/null"});
            process.waitFor();
            if (process.exitValue() == 0) {
                String result = new String(process.getInputStream().readAllBytes()).trim();
                String[] parts = result.split("\\s+");
                if (parts.length >= 2) {
                    int width = Integer.parseInt(parts[1]);
                    if (width > RESERVED_SPACE) {
                        return width;
                    }
                }
            }
        } catch (Exception e) {
            // Fall through to default
        }
        
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
        
        // Calculate bar width
        int barWidth = terminalWidth - RESERVED_SPACE;
        if (barWidth < 10) {
            barWidth = 10; // Minimum bar width
        }
        
        // Calculate filled portion
        int filled = (barWidth * current) / total;
        if (filled > barWidth) {
            filled = barWidth;
        }
        
        // Build progress bar
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < filled; i++) {
            bar.append("*");
        }
        for (int i = filled; i < barWidth; i++) {
            bar.append(" ");
        }
        
        // Print complete progress line
        System.out.printf("[%-20s] %s %3d%% (%d/%d)", 
            truncatedPhase, bar.toString(), percentage, current, total);
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