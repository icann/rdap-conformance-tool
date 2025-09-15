package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.NetworkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Context object for isolated IP version execution.
 * Manages thread-local System properties to enable parallel IPv4/IPv6 execution.
 */
public class IPVersionContext {
    private static final Logger logger = LoggerFactory.getLogger(IPVersionContext.class);
    
    private final NetworkProtocol protocol;
    private final Map<String, String> originalSystemProps = new HashMap<>();
    private static final ThreadLocal<IPVersionContext> currentContext = new ThreadLocal<>();
    
    // System properties that control IP version preferences
    private static final String PREFER_IPV6_ADDRESSES = "java.net.preferIPv6Addresses";
    private static final String PREFER_IPV6_STACK = "java.net.preferIPv6Stack";
    private static final String PREFER_IPV4_ADDRESSES = "java.net.preferIPv4Addresses";
    private static final String PREFER_IPV4_STACK = "java.net.preferIPv4Stack";
    
    public IPVersionContext(NetworkProtocol protocol) {
        this.protocol = protocol;
    }
    
    /**
     * Activates this context for the current thread.
     * Sets System properties to prefer the configured IP version.
     */
    public void activate() {
        logger.debug("Activating IPVersionContext for {}", protocol);
        currentContext.set(this);
        
        // Save original system properties
        saveSystemProperties();
        
        // Set properties for this IP version
        if (protocol == NetworkProtocol.IPv6) {
            System.setProperty(PREFER_IPV6_ADDRESSES, "true");
            System.setProperty(PREFER_IPV6_STACK, "true");
            System.setProperty(PREFER_IPV4_ADDRESSES, "false");
            System.setProperty(PREFER_IPV4_STACK, "false");
            logger.debug("Set System properties to prefer IPv6");
        } else {
            System.setProperty(PREFER_IPV4_ADDRESSES, "true");
            System.setProperty(PREFER_IPV4_STACK, "true");
            System.setProperty(PREFER_IPV6_ADDRESSES, "false");
            System.setProperty(PREFER_IPV6_STACK, "false");
            logger.debug("Set System properties to prefer IPv4");
        }
    }
    
    /**
     * Deactivates this context, restoring original System properties.
     */
    public void deactivate() {
        logger.debug("Deactivating IPVersionContext for {}", protocol);
        try {
            restoreSystemProperties();
        } finally {
            currentContext.remove();
        }
    }
    
    /**
     * Gets the current IPVersionContext for this thread.
     * @return the current context, or null if none is active
     */
    public static IPVersionContext current() {
        return currentContext.get();
    }
    
    /**
     * Gets the network protocol for this context.
     */
    public NetworkProtocol getProtocol() {
        return protocol;
    }
    
    /**
     * Checks if this context is currently active for this thread.
     */
    public boolean isActive() {
        return currentContext.get() == this;
    }
    
    /**
     * Saves current System properties so they can be restored later.
     */
    private void saveSystemProperties() {
        originalSystemProps.put(PREFER_IPV6_ADDRESSES, 
            System.getProperty(PREFER_IPV6_ADDRESSES, "false"));
        originalSystemProps.put(PREFER_IPV6_STACK, 
            System.getProperty(PREFER_IPV6_STACK, "false"));
        originalSystemProps.put(PREFER_IPV4_ADDRESSES, 
            System.getProperty(PREFER_IPV4_ADDRESSES, "true"));
        originalSystemProps.put(PREFER_IPV4_STACK, 
            System.getProperty(PREFER_IPV4_STACK, "false"));
        
        logger.debug("Saved original System properties: {}", originalSystemProps);
    }
    
    /**
     * Restores System properties to their original values.
     */
    private void restoreSystemProperties() {
        originalSystemProps.forEach((key, value) -> {
            if (value != null) {
                System.setProperty(key, value);
            } else {
                System.clearProperty(key);
            }
        });
        logger.debug("Restored System properties to original values");
    }
    
    @Override
    public String toString() {
        return "IPVersionContext{protocol=" + protocol + ", active=" + isActive() + "}";
    }
}