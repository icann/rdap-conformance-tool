package org.icann.rdapconformance.validator.workflow.profile;

import org.icann.rdapconformance.validator.NetworkProtocol;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IPVersionContextTest {
    
    @AfterMethod
    public void cleanup() {
        // Clear any active context after each test
        IPVersionContext current = IPVersionContext.current();
        if (current != null) {
            current.deactivate();
        }
    }
    
    @Test
    public void testIPVersionContext_IPv4Activation() {
        IPVersionContext context = new IPVersionContext(NetworkProtocol.IPv4);
        
        assertThat(context.getProtocol()).isEqualTo(NetworkProtocol.IPv4);
        assertThat(context.isActive()).isFalse();
        
        context.activate();
        
        assertThat(context.isActive()).isTrue();
        assertThat(IPVersionContext.current()).isEqualTo(context);
        
        context.deactivate();
        
        assertThat(context.isActive()).isFalse();
        assertThat(IPVersionContext.current()).isNull();
    }
    
    @Test
    public void testIPVersionContext_IPv6Activation() {
        IPVersionContext context = new IPVersionContext(NetworkProtocol.IPv6);
        
        assertThat(context.getProtocol()).isEqualTo(NetworkProtocol.IPv6);
        assertThat(context.isActive()).isFalse();
        
        context.activate();
        
        assertThat(context.isActive()).isTrue();
        assertThat(IPVersionContext.current()).isEqualTo(context);
        
        context.deactivate();
        
        assertThat(context.isActive()).isFalse();
        assertThat(IPVersionContext.current()).isNull();
    }
    
    @Test
    public void testIPVersionContext_ContextLifecycle() {
        IPVersionContext context = new IPVersionContext(NetworkProtocol.IPv6);
        
        // Verify context lifecycle works correctly
        assertThat(context.isActive()).isFalse();
        assertThat(IPVersionContext.current()).isNull();
        
        context.activate();
        assertThat(context.isActive()).isTrue();
        assertThat(IPVersionContext.current()).isEqualTo(context);
        
        context.deactivate();
        assertThat(context.isActive()).isFalse();
        assertThat(IPVersionContext.current()).isNull();
    }
    
    @Test
    public void testIPVersionContext_MultipleContexts() {
        IPVersionContext context1 = new IPVersionContext(NetworkProtocol.IPv4);
        IPVersionContext context2 = new IPVersionContext(NetworkProtocol.IPv6);
        
        // Only one context can be active per thread
        context1.activate();
        assertThat(IPVersionContext.current()).isEqualTo(context1);
        
        context2.activate();
        assertThat(IPVersionContext.current()).isEqualTo(context2);
        assertThat(context1.isActive()).isFalse();
        assertThat(context2.isActive()).isTrue();
        
        context2.deactivate();
        assertThat(IPVersionContext.current()).isNull();
    }
    
    @Test
    public void testIPVersionContext_ToString() {
        IPVersionContext context = new IPVersionContext(NetworkProtocol.IPv4);
        
        String str = context.toString();
        assertThat(str).contains("IPv4");
        assertThat(str).contains("active=false");
        
        context.activate();
        str = context.toString();
        assertThat(str).contains("IPv4");
        assertThat(str).contains("active=true");
        
        context.deactivate();
    }
}