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
    private String lastPhase = null;
    
    // Pulsing animation support
    private long lastUpdateTime = 0;
    private int pulseState = 0;
    private static final long PULSE_INTERVAL_MS = 500; // 0.5 seconds between pulses
    
    // ASCII fallback spinner
    private static final String[] ASCII_SPINNER = {"|", "/", "-", "\\"};
    
    // UTF-8 enhanced spinners
    private static final String[] UTF8_BRAILLE_SPINNER = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static final String[] UTF8_INTENSITY_SPINNER = {"·", "•", "✛", "✚", "✦", "✶", "✳"}; // Building energy progression
    
    // ANSI color codes
    private static final String GREEN = "\u001b[32m";
    private static final String YELLOW = "\u001b[33m";
    private static final String BLUE = "\u001b[34m";
    private static final String PURPLE = "\u001b[35m";
    private static final String RESET = "\u001b[0m";
    
    // Capability detection
    private final boolean supportsUTF8;
    private final boolean supportsColor;
    private final String[] pulseChars;
    
    public ProgressDisplay() {
        this.terminalSupported = isTerminalSupported();
        this.terminalWidth = getTerminalWidth();
        this.supportsUTF8 = detectUTF8Support();
        this.supportsColor = detectColorSupport();
        this.pulseChars = chooseBestSpinner();
    }
    
    /**
     * Update the progress bar display.
     * Only updates if percentage has changed to avoid excessive console writes.
     */
    public synchronized void updateProgress(String phase, int current, int total) {
        if (!terminalSupported || total <= 0) {
            return;
        }
        
        int percentage = (current * 100) / total;
        long currentTime = System.currentTimeMillis();
        
        // Check if we should pulse (update display even if percentage/phase unchanged)
        boolean shouldPulse = (currentTime - lastUpdateTime) >= PULSE_INTERVAL_MS;
        
        // Only update if percentage changed, phase changed, or it's time to pulse
        if (percentage == lastPercentage && java.util.Objects.equals(phase, lastPhase) && !shouldPulse) {
            return;
        }
        
        // Update pulse state if we're pulsing
        if (shouldPulse) {
            pulseState = (pulseState + 1) % pulseChars.length;
            lastUpdateTime = currentTime;
        }
        
        lastPercentage = percentage;
        lastPhase = phase;
        
        clearCurrentLine();
        printProgressBar(phase, current, total, percentage);
        System.out.flush();
    }
    
    /**
     * Clear the progress bar and move to next line.
     * Call this when progress is complete or on error.
     */
    public synchronized void clearAndFinish() {
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
                if (width >= RESERVED_SPACE) {  // Ensure sufficient terminal width for progress bar display
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
        System.out.flush();
    }
    
    /**
     * Print the progress bar with specified format.
     */
    private void printProgressBar(String phase, int current, int total, int percentage) {
        // Truncate phase name if too long
        String truncatedPhase = truncatePhase(phase);
        
        // Format the right-side info
        String rightInfo = String.format("%3d%% (%d/%d)", percentage, current, total);
        
        // Get current pulse character for left position
        String currentPulseChar = pulseChars[pulseState];
        
        // Build the left part with pulsing character after '[': [spinner PhaseName     ]
        String leftPart;
        if (supportsColor) {
            leftPart = String.format("[%s%s%s %-19s] ", PURPLE, currentPulseChar, RESET, truncatedPhase);
        } else {
            leftPart = String.format("[%s %-19s] ", currentPulseChar, truncatedPhase);
        }
        
        // Calculate exact space available for asterisks
        int availableWidth = terminalWidth - leftPart.length() - rightInfo.length();
        int barWidth = Math.max(1, availableWidth);
        
        // Calculate filled portion
        int filled = (barWidth * current) / total;
        if (filled > barWidth) {
            filled = barWidth;
        }
        
        // Build the asterisk bar - all asterisks are static '*'
        StringBuilder bar = new StringBuilder();
        
        // All asterisks are static '*' characters
        for (int i = 0; i < filled; i++) {
            if (supportsColor) {
                bar.append(GREEN).append("*").append(RESET);
            } else {
                bar.append("*");
            }
        }
        for (int i = filled; i < barWidth; i++) {
            bar.append(" ");
        }
        
        // Build complete line
        String completeLine = leftPart + bar.toString() + rightInfo;
        
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
    
    /**
     * Detect UTF-8 support across platforms.
     */
    private boolean detectUTF8Support() {
        // Check system file encoding
        String encoding = System.getProperty("file.encoding", "").toLowerCase();
        if (encoding.contains("utf")) {
            return true;
        }
        
        // Check LANG environment variable (Unix/Linux/macOS)
        String lang = System.getenv("LANG");
        if (lang != null && lang.toLowerCase().contains("utf")) {
            return true;
        }
        
        // Check LC_ALL environment variable
        String lcAll = System.getenv("LC_ALL");
        if (lcAll != null && lcAll.toLowerCase().contains("utf")) {
            return true;
        }
        
        // Modern Windows terminals usually support UTF-8
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("windows")) {
            // Windows Terminal, PowerShell 7+, modern cmd support UTF-8
            String termProgram = System.getenv("WT_SESSION");
            if (termProgram != null) {
                return true; // Windows Terminal
            }
        }
        
        return false; // Default to safe ASCII
    }
    
    /**
     * Detect ANSI color support across platforms.
     */
    private boolean detectColorSupport() {
        if (!terminalSupported) {
            return false; // No colors if not interactive
        }
        
        // Check TERM environment variable
        String term = System.getenv("TERM");
        if (term != null) {
            String termLower = term.toLowerCase();
            if (termLower.contains("color") || 
                termLower.contains("xterm") || 
                termLower.contains("screen") ||
                termLower.contains("tmux")) {
                return true;
            }
        }
        
        // Check for color-supporting environment variables
        if (System.getenv("COLORTERM") != null) {
            return true;
        }
        
        // Check terminal programs that support color
        String termProgram = System.getenv("TERM_PROGRAM");
        if (termProgram != null) {
            String programLower = termProgram.toLowerCase();
            if (programLower.contains("iterm") ||
                programLower.contains("terminal") ||
                programLower.contains("vscode")) {
                return true;
            }
        }
        
        // Windows Terminal supports colors
        if (System.getenv("WT_SESSION") != null) {
            return true;
        }
        
        return false; // Default to no colors
    }
    
    /**
     * Choose the best spinner based on terminal capabilities.
     */
    private String[] chooseBestSpinner() {
        if (supportsUTF8) {
            // Use the intensity-based spinner for UTF-8 capable terminals
            return UTF8_INTENSITY_SPINNER;
        } else {
            // Fall back to ASCII spinner
            return ASCII_SPINNER;
        }
    }
}