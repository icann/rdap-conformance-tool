package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.NetworkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Context object for isolated IP version execution.
 * Provides thread-local IP version context to enable parallel IPv4/IPv6 execution.
 */
public class IPVersionContext {
    private static final Logger logger = LoggerFactory.getLogger(IPVersionContext.class);
    
    private final NetworkProtocol protocol;
    private static final ThreadLocal<IPVersionContext> currentContext = new ThreadLocal<>();
    
    public IPVersionContext(NetworkProtocol protocol) {
        this.protocol = protocol;
    }
    
    /**
     * Activates this context for the current thread.
     */
    public void activate() {
        logger.debug("Activating IPVersionContext for {}", protocol);
        currentContext.set(this);
    }
    
    /**
     * Deactivates this context.
     */
    public void deactivate() {
        logger.debug("Deactivating IPVersionContext for {}", protocol);
        currentContext.remove();
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
    
    /*
     * NOTE: We previously manipulated java.net.preferIPv4Addresses
     * and java.net.preferIPv6Addresses here, but they have absolutely no effect when
     * changed at runtime. The JVM networking stack  reads these once at startup and that's it.
     * Instead, we switch the IP version through explicit the local address
     * binding in LocalBindRoutePlanner.
     * 
     * If Oracle/OpenJDK ever implements runtime-effective IP preference system properties,
     * we could just flip the vars back and forth.
     */
    
    @Override
    public String toString() {
        return "IPVersionContext{protocol=" + protocol + ", active=" + isActive() + "}";
    }
}