package org.icann.rdapconformance.validator.session;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Background service for automatic cleanup of expired sessions in concurrent RDAP validation environments.
 *
 * <p>This service runs as a daemon thread and periodically scans for expired sessions,
 * automatically cleaning them up to prevent memory leaks and maintain optimal performance
 * in long-running web applications handling 200+ concurrent sessions.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Configurable cleanup intervals (default: 5 minutes)</li>
 *   <li>Graceful startup and shutdown handling</li>
 *   <li>Error resilience with continued operation on cleanup failures</li>
 *   <li>Performance monitoring and logging</li>
 *   <li>Thread-safe singleton implementation</li>
 *   <li>Integration with SessionManager for coordinated cleanup</li>
 * </ul>
 *
 * <p>The service is designed to run continuously in web application environments
 * and automatically starts when first accessed. It uses a single daemon thread
 * to minimize resource usage while providing reliable cleanup functionality.</p>
 *
 * @see SessionManager
 * @since 3.0.0
 */
public class SessionCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(SessionCleanupService.class);

    /**
     * Default cleanup interval in minutes
     */
    public static final int DEFAULT_CLEANUP_INTERVAL_MINUTES = 5;

    /**
     * Singleton instance using volatile for thread-safe lazy initialization
     */
    private static volatile SessionCleanupService instance;

    /**
     * Scheduled executor for running cleanup tasks
     */
    private volatile ScheduledExecutorService scheduler;

    /**
     * Service state tracking
     */
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /**
     * Configuration settings
     */
    private volatile int cleanupIntervalMinutes = DEFAULT_CLEANUP_INTERVAL_MINUTES;

    /**
     * Private constructor for singleton pattern
     */
    private SessionCleanupService() {
    }

    /**
     * Returns the singleton instance using double-checked locking for optimal performance.
     *
     * @return the SessionCleanupService singleton instance
     */
    public static SessionCleanupService getInstance() {
        SessionCleanupService result = instance;
        if (result == null) {
            synchronized (SessionCleanupService.class) {
                result = instance;
                if (result == null) {
                    instance = result = new SessionCleanupService();
                }
            }
        }
        return result;
    }

    /**
     * Starts the automatic session cleanup service.
     * This method is idempotent and safe to call multiple times.
     */
    public void start() {
        if (isShutdown.get()) {
            throw new IllegalStateException("Cannot start - service has been shut down");
        }

        if (isRunning.compareAndSet(false, true)) {
            logger.info("Starting session cleanup service with {} minute intervals", cleanupIntervalMinutes);

            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "SessionCleanupService");
                t.setDaemon(true); // Don't prevent JVM shutdown
                t.setPriority(Thread.MIN_PRIORITY); // Low priority background task
                return t;
            });

            scheduler.scheduleAtFixedRate(
                this::performCleanup,
                cleanupIntervalMinutes, // Initial delay
                cleanupIntervalMinutes, // Period
                TimeUnit.MINUTES
            );

            logger.info("Session cleanup service started successfully");
        } else {
            logger.debug("Session cleanup service is already running");
        }
    }

    /**
     * Stops the automatic session cleanup service gracefully.
     * This method is idempotent and safe to call multiple times.
     */
    public void stop() {
        if (isRunning.compareAndSet(true, false)) {
            logger.info("Stopping session cleanup service");

            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    // Wait up to 30 seconds for clean shutdown
                    if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                        logger.warn("Session cleanup service did not terminate gracefully, forcing shutdown");
                        scheduler.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    logger.warn("Interrupted while waiting for session cleanup service shutdown");
                    scheduler.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                scheduler = null;
            }

            logger.info("Session cleanup service stopped");
        } else {
            logger.debug("Session cleanup service is not running");
        }
    }

    /**
     * Permanently shuts down the service. After calling this method,
     * the service cannot be restarted.
     */
    public void shutdown() {
        stop();
        isShutdown.set(true);
        logger.info("Session cleanup service shut down permanently");
    }

    /**
     * Performs a manual cleanup of expired sessions.
     * This can be called independently of the automatic cleanup schedule.
     *
     * @return the number of sessions cleaned up
     */
    public int cleanupNow() {
        return performCleanup();
    }

    /**
     * Checks if the cleanup service is currently running.
     *
     * @return true if the service is running
     */
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * Checks if the cleanup service has been permanently shut down.
     *
     * @return true if the service has been shut down
     */
    public boolean isShutdown() {
        return isShutdown.get();
    }

    /**
     * Sets the cleanup interval in minutes.
     * If the service is running, it will be restarted with the new interval.
     *
     * @param intervalMinutes the cleanup interval in minutes
     */
    public void setCleanupInterval(int intervalMinutes) {
        if (intervalMinutes <= 0) {
            throw new IllegalArgumentException("Cleanup interval must be positive");
        }

        this.cleanupIntervalMinutes = intervalMinutes;
        logger.info("Cleanup interval updated to {} minutes", intervalMinutes);

        // Restart service with new interval if currently running
        if (isRunning.get()) {
            stop();
            start();
        }
    }

    /**
     * Gets the current cleanup interval in minutes.
     *
     * @return the cleanup interval
     */
    public int getCleanupInterval() {
        return cleanupIntervalMinutes;
    }

    /**
     * Performs the actual cleanup operation.
     * This method is designed to be resilient to errors and continue operation.
     *
     * @return the number of sessions cleaned up
     */
    private int performCleanup() {
        try {
            long startTime = System.currentTimeMillis();
            SessionManager sessionManager = SessionManager.getInstance();

            int initialCount = sessionManager.getActiveSessionCount();
            int cleanedUp = sessionManager.cleanExpiredSessions();
            int finalCount = sessionManager.getActiveSessionCount();

            long duration = System.currentTimeMillis() - startTime;

            if (cleanedUp > 0) {
                logger.info("Session cleanup completed: {} sessions removed, {} active sessions remaining (took {} ms)",
                    cleanedUp, finalCount, duration);
            } else {
                logger.debug("Session cleanup completed: no expired sessions found, {} active sessions (took {} ms)",
                    finalCount, duration);
            }

            // Log warning if session count is getting high
            if (finalCount > sessionManager.getMaxSessions() * 0.8) {
                logger.warn("High session count detected: {}/{} sessions active",
                    finalCount, sessionManager.getMaxSessions());
            }

            return cleanedUp;

        } catch (Exception e) {
            logger.error("Error during session cleanup - continuing operation", e);
            return 0;
        }
    }
}