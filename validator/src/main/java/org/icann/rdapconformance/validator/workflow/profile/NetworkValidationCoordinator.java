package org.icann.rdapconformance.validator.workflow.profile;

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
    
    // Rate limiting configuration - very conservative settings to avoid server rate limiting
    private static final int MAX_CONCURRENT_NETWORK_VALIDATIONS = 1; // Ultra-conservative: only 1 at a time
    private static final long RATE_LIMIT_DELAY_MS = 500; // 500ms between network operations
    
    // Fallback mechanism - disabled by default due to connection tracking issues
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
                        boolean result = validation.validate();
                        
                        logger.debug("Completed network validation: {} - Result: {}", 
                            validation.getGroupName(), result);
                        
                        return result;
                        
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
            
            // Wait for all validations to complete with shorter timeout
            CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            
            // Wait with shorter timeout to prevent hanging (2 minutes max)
            try {
                allOf.get(2, TimeUnit.MINUTES);
            } catch (java.util.concurrent.TimeoutException e) {
                logger.error("Network validations timed out after 2 minutes - cancelling remaining operations");
                futures.forEach(future -> future.cancel(true));
                return false;
            }
            
            // Check if all validations passed
            boolean allPassed = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        logger.error("Error getting validation result", e);
                        return false;
                    }
                })
                .allMatch(result -> result);
            
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
        
        List<ProfileValidation> lightweight = networkValidations.stream()
            .filter(v -> isLightweightNetworkValidation(v))
            .collect(Collectors.toList());
        
        List<ProfileValidation> ssl = networkValidations.stream()
            .filter(v -> isSSLValidation(v))
            .collect(Collectors.toList());
        
        List<ProfileValidation> other = networkValidations.stream()
            .filter(v -> !isLightweightNetworkValidation(v) && !isSSLValidation(v))
            .collect(Collectors.toList());
        
        return new NetworkValidationGroups(lightweight, ssl, other);
    }
    
    private static boolean isLightweightNetworkValidation(ProfileValidation validation) {
        String className = validation.getClass().getSimpleName();
        return className.equals("TigValidation1Dot13") || // Header analysis
               className.equals("TigValidation1Dot11Dot1") || // URL validation
               className.equals("ResponseValidationTestInvalidRedirect_2024") ||
               className.equals("ResponseValidationHelp_2024") ||
               className.equals("ResponseValidationDomainInvalid_2024");
    }
    
    private static boolean isSSLValidation(ProfileValidation validation) {
        String className = validation.getClass().getSimpleName();
        return className.equals("TigValidation1Dot2") || // SSL network
               className.equals("TigValidation1Dot5_2024"); // SSL network
    }
    
    /**
     * Groups network validations by their execution characteristics.
     */
    public static class NetworkValidationGroups {
        public final List<ProfileValidation> lightweight;
        public final List<ProfileValidation> ssl;
        public final List<ProfileValidation> other;
        
        public NetworkValidationGroups(List<ProfileValidation> lightweight, 
                                     List<ProfileValidation> ssl, 
                                     List<ProfileValidation> other) {
            this.lightweight = lightweight;
            this.ssl = ssl;
            this.other = other;
        }
        
        public boolean isEmpty() {
            return lightweight.isEmpty() && ssl.isEmpty() && other.isEmpty();
        }
    }
    
    /**
     * Shutdown the network validation executor.
     * Should be called when the application is shutting down.
     */
    public static void shutdown() {
        networkExecutor.shutdown();
        try {
            if (!networkExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                networkExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            networkExecutor.shutdownNow();
        }
    }
}