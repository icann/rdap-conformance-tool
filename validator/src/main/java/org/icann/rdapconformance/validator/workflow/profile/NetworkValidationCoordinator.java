package org.icann.rdapconformance.validator.workflow.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates parallel execution of network validations with rate limiting.
 * Preserves IPv4/IPv6 stack switching behavior while allowing safe parallelization.
 */
public class NetworkValidationCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(NetworkValidationCoordinator.class);
    
    // Conservative rate limiting configuration when explicitly enabled
    private static final int MAX_CONCURRENT_NETWORK_VALIDATIONS = 2; // Conservative: 2 concurrent connections
    private static final long RATE_LIMIT_DELAY_MS = 200; // 200ms between network operations (conservative)
    
    // Opt-in mechanism - user must explicitly enable parallel networking
    private static final boolean PARALLEL_NETWORK_ENABLED = 
        "true".equals(System.getProperty("rdap.parallel.network", "false"));
    
    // Thread pool for network validations
    private static final ExecutorService networkExecutor = 
        Executors.newFixedThreadPool(MAX_CONCURRENT_NETWORK_VALIDATIONS, 
            r -> {
                Thread t = new Thread(r, "NetworkValidation-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            });
    
    // Separate thread pool for timeout-prone validations (HTTP + slow HTTPS)
    private static final ExecutorService httpExecutor = 
        Executors.newFixedThreadPool(2, // 2 threads for timeout-prone validations
            r -> {
                Thread t = new Thread(r, "TimeoutProneValidation-" + System.currentTimeMillis());
                t.setDaemon(true);
                return t;
            });
    
    // Semaphore for rate limiting
    private static final Semaphore rateLimitSemaphore = new Semaphore(MAX_CONCURRENT_NETWORK_VALIDATIONS);
    
    // Additional rate limiting for aggressive network operations
    private static long lastNetworkOperationTime = 0;
    private static final Object rateLimitLock = new Object();
    
    /**
     * Executes network validations with rate limiting and controlled concurrency.
     * 
     * IMPORTANT: This method preserves the existing IPv4/IPv6 stack coordination by:
     * 1. Maintaining global NetworkInfo state for each validation
     * 2. Using controlled concurrency to avoid overwhelming servers
     * 3. Applying rate limiting between network operations
     * 
     * @param networkValidations List of network validations to execute
     * @return true if all validations passed, false otherwise
     */
    public static boolean executeNetworkValidations(List<ProfileValidation> networkValidations) {
        if (networkValidations == null || networkValidations.isEmpty()) {
            return true;
        }
        
        // Fallback to sequential execution if parallel networking is disabled
        if (!PARALLEL_NETWORK_ENABLED) {
            logger.info("Executing {} network validations sequentially (parallel disabled)", networkValidations.size());
            boolean result = true;
            for (ProfileValidation validation : networkValidations) {
                logger.info("Validating (sequential): {}", validation.getGroupName());
                result &= validation.validate();
            }
            return result;
        }
        
        logger.info("Executing {} network validations with controlled parallelization and rate limiting", networkValidations.size());
        
        try {
            // Execute validations with controlled concurrency and rate limiting
            List<CompletableFuture<Boolean>> futures = networkValidations.stream()
                .map(validation -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // Acquire rate limit permit
                        rateLimitSemaphore.acquire();
                        
                        // Apply global rate limiting to prevent overwhelming servers
                        synchronized (rateLimitLock) {
                            long currentTime = System.currentTimeMillis();
                            long timeSinceLastOperation = currentTime - lastNetworkOperationTime;
                            
                            if (timeSinceLastOperation < RATE_LIMIT_DELAY_MS) {
                                long sleepTime = RATE_LIMIT_DELAY_MS - timeSinceLastOperation;
                                Thread.sleep(sleepTime);
                            }
                            
                            lastNetworkOperationTime = System.currentTimeMillis();
                        }
                        
                        logger.debug("Executing network validation: {}", validation.getGroupName());
                        
                        // Execute the validation
                        // NOTE: The validation will use the current NetworkInfo state (IPv4/IPv6)
                        // which is set by the caller before invoking this method
                        // CRITICAL: We do NOT modify NetworkInfo here - it's set globally before this method
                        try {
                            boolean result = validation.validate();
                            logger.debug("Network validation completed: {} - Result: {}", 
                                validation.getGroupName(), result);
                            return result;
                        } catch (Exception validationError) {
                            logger.error("Network validation failed: {}", validation.getGroupName(), validationError);
                            return false;
                        }
                        
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.error("Network validation interrupted: {}", validation.getGroupName(), e);
                        return false;
                    } catch (Exception e) {
                        logger.error("Network validation failed: {}", validation.getGroupName(), e);
                        return false;
                    } finally {
                        // Release rate limit permit
                        rateLimitSemaphore.release();
                    }
                }, networkExecutor))
                .collect(Collectors.toList());
            
            // Wait for all validations to complete with generous timeout
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            
            // Wait with generous timeout - network operations can be slow
            try {
                allOf.get(5, TimeUnit.MINUTES);
            } catch (java.util.concurrent.TimeoutException e) {
                logger.warn("Network validations timed out after 5 minutes - some operations may still be in progress");
                // Don't cancel futures - let them complete naturally
                // Individual validations have their own timeouts
            }
            
            // Check if all validations passed
            boolean allPassed = true;
            for (CompletableFuture<Boolean> future : futures) {
                try {
                    if (future.isDone()) {
                        Boolean result = future.get(100, TimeUnit.MILLISECONDS);
                        if (result != null) {
                            allPassed &= result;
                        }
                    } else {
                        logger.warn("Network validation future not completed: {}", future);
                        allPassed = false;
                    }
                } catch (Exception e) {
                    logger.error("Error getting validation result", e);
                    allPassed = false;
                }
            }
            
            logger.info("Network validations completed - All passed: {}", allPassed);
            return allPassed;
            
        } catch (Exception e) {
            logger.error("Error executing network validations", e);
            return false;
        }
    }
    
    /**
     * Categorizes network validations by their characteristics for optimal execution.
     * 
     * @param networkValidations List of all network validations
     * @return NetworkValidationGroups containing categorized validations
     */
    public static NetworkValidationGroups categorizeNetworkValidations(List<ProfileValidation> networkValidations) {
        if (networkValidations == null || networkValidations.isEmpty()) {
            return new NetworkValidationGroups(List.of(), List.of(), List.of());
        }
        
        List<ProfileValidation> httpValidations = networkValidations.stream()
            .filter(v -> isHttpValidation(v))
            .collect(Collectors.toList());
        
        List<ProfileValidation> httpsValidations = networkValidations.stream()
            .filter(v -> !isHttpValidation(v))
            .collect(Collectors.toList());
        
        // For compatibility, return HTTP validations as 'lightweight', HTTPS as 'ssl', empty as 'other'
        return new NetworkValidationGroups(httpValidations, httpsValidations, List.of());
    }
    
    /**
     * Identifies validations that are timeout-prone and should run asynchronously.
     * This includes both HTTP calls and slow HTTPS endpoints like /help.
     */
    private static boolean isHttpValidation(ProfileValidation validation) {
        String className = validation.getClass().getSimpleName();
        // TigValidation1Dot2 makes HTTP calls by converting HTTPS to HTTP
        // ResponseValidationHelp_2024 often times out on /help endpoints
        // ResponseValidationDomainInvalid_2024 can be slow on /domain/not-a-domain.invalid
        return className.equals("TigValidation1Dot2") ||
               className.equals("ResponseValidationHelp_2024") ||
               className.equals("ResponseValidationDomainInvalid_2024");
    }
    
    /**
     * Legacy method for backward compatibility
     */
    private static boolean isLightweightNetworkValidation(ProfileValidation validation) {
        return isHttpValidation(validation);
    }
    
    /**
     * Legacy method for backward compatibility  
     */
    private static boolean isSSLValidation(ProfileValidation validation) {
        return !isHttpValidation(validation);
    }
    
    /**
     * Groups network validations by their execution characteristics.
     * lightweight = Timeout-prone validations (HTTP + slow HTTPS like /help)
     * ssl = Normal HTTPS validations (fast execution)
     * other = unused (kept for compatibility)
     */
    public static class NetworkValidationGroups {
        public final List<ProfileValidation> lightweight; // HTTP validations
        public final List<ProfileValidation> ssl;         // HTTPS validations
        public final List<ProfileValidation> other;       // Unused
        
        public NetworkValidationGroups(List<ProfileValidation> lightweight, 
                                     List<ProfileValidation> ssl, 
                                     List<ProfileValidation> other) {
            this.lightweight = lightweight;
            this.ssl = ssl;
            this.other = other;
        }
        
        public List<ProfileValidation> getTimeoutProneValidations() {
            return lightweight;
        }
        
        public List<ProfileValidation> getNormalValidations() {
            return ssl;
        }
        
        // Legacy methods for backward compatibility
        public List<ProfileValidation> getHttpValidations() {
            return lightweight;
        }
        
        public List<ProfileValidation> getHttpsValidations() {
            return ssl;
        }
        
        public boolean isEmpty() {
            return lightweight.isEmpty() && ssl.isEmpty() && other.isEmpty();
        }
    }
    
    /**
     * Executes timeout-prone and normal validations with appropriate threading strategy.
     * Timeout-prone validations run asynchronously in a separate thread pool to avoid blocking.
     * Normal validations run in the main network thread pool.
     * 
     * @param httpValidations List of timeout-prone validations (HTTP + slow HTTPS like /help)
     * @param httpsValidations List of normal validations (fast HTTPS execution)
     * @param timeoutSeconds Timeout in seconds for all validations (respects config timeout)
     * @return true if all validations passed, false otherwise
     */
    public static boolean executeHttpAndHttpsValidations(List<ProfileValidation> httpValidations, 
                                                         List<ProfileValidation> httpsValidations,
                                                         int timeoutSeconds) {
        if ((httpValidations == null || httpValidations.isEmpty()) && 
            (httpsValidations == null || httpsValidations.isEmpty())) {
            return true;
        }
        
        logger.info("Executing {} timeout-prone validations async, {} normal validations sync", 
                   httpValidations != null ? httpValidations.size() : 0,
                   httpsValidations != null ? httpsValidations.size() : 0);
        
        List<CompletableFuture<Boolean>> allFutures = new ArrayList<>();
        
        // Submit timeout-prone validations to separate thread pool (async, non-blocking)
        if (httpValidations != null && !httpValidations.isEmpty()) {
            for (ProfileValidation validation : httpValidations) {
                CompletableFuture<Boolean> timeoutProneFuture = CompletableFuture.supplyAsync(() -> {
                    logger.debug("Executing timeout-prone validation: {}", validation.getGroupName());
                    try {
                        boolean result = validation.validate();
                        logger.debug("Timeout-prone validation completed: {} - Result: {}", 
                                   validation.getGroupName(), result);
                        return result;
                    } catch (Exception e) {
                        logger.error("Timeout-prone validation failed: {}", validation.getGroupName(), e);
                        return false;
                    }
                }, httpExecutor);
                allFutures.add(timeoutProneFuture);
            }
        }
        
        // Execute normal validations with rate limiting (using existing network pool)
        if (httpsValidations != null && !httpsValidations.isEmpty()) {
            boolean normalResult = executeNetworkValidations(httpsValidations);
            // Convert normal result to future for consistent handling
            CompletableFuture<Boolean> normalFuture = CompletableFuture.completedFuture(normalResult);
            allFutures.add(normalFuture);
        }
        
        // Wait for all validations to complete
        try {
            CompletableFuture<Void> allOf = CompletableFuture.allOf(allFutures.toArray(new CompletableFuture[0]));
            
            // Wait with configured timeout plus small buffer for coordination overhead
            int coordinationTimeoutSeconds = Math.max(timeoutSeconds + 5, 30); // At least 30 seconds, or config timeout + 5 seconds buffer
            allOf.get(coordinationTimeoutSeconds, TimeUnit.SECONDS);
            
            // Check if all validations passed
            boolean allPassed = true;
            for (CompletableFuture<Boolean> future : allFutures) {
                Boolean result = future.get(100, TimeUnit.MILLISECONDS);
                if (result != null) {
                    allPassed &= result;
                } else {
                    allPassed = false;
                }
            }
            
            logger.info("Timeout-prone/Normal validations completed - All passed: {}", allPassed);
            return allPassed;
            
        } catch (Exception e) {
            logger.error("Error executing timeout-prone/normal validations", e);
            return false;
        }
    }
    
    /**
     * Shutdown the network validation executors.
     * Should be called when the application is shutting down.
     */
    public static void shutdown() {
        networkExecutor.shutdown();
        httpExecutor.shutdown();
        try {
            if (!networkExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                networkExecutor.shutdownNow();
            }
            if (!httpExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                httpExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            networkExecutor.shutdownNow();
            httpExecutor.shutdownNow();
        }
    }
}