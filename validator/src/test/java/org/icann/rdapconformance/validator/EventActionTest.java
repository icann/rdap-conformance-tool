package org.icann.rdapconformance.validator;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class EventActionTest {

    @Test
    public void testEnumValues() {
        EventAction[] values = EventAction.values();
        
        assertThat(values).hasSize(3);
        assertThat(values).contains(
            EventAction.LAST_UPDATE_OF_RDAP_DATABASE,
            EventAction.REGISTRATION,
            EventAction.EXPIRATION
        );
    }
    
    @Test
    public void testLastUpdateOfRdapDatabase() {
        EventAction action = EventAction.LAST_UPDATE_OF_RDAP_DATABASE;
        
        assertThat(action.type).isEqualTo("last update of RDAP database");
        assertThat(action.name()).isEqualTo("LAST_UPDATE_OF_RDAP_DATABASE");
    }
    
    @Test
    public void testRegistration() {
        EventAction action = EventAction.REGISTRATION;
        
        assertThat(action.type).isEqualTo("registration");
        assertThat(action.name()).isEqualTo("REGISTRATION");
    }
    
    @Test
    public void testExpiration() {
        EventAction action = EventAction.EXPIRATION;
        
        assertThat(action.type).isEqualTo("expiration");
        assertThat(action.name()).isEqualTo("EXPIRATION");
    }
    
    @Test
    public void testValueOf() {
        assertThat(EventAction.valueOf("LAST_UPDATE_OF_RDAP_DATABASE"))
            .isEqualTo(EventAction.LAST_UPDATE_OF_RDAP_DATABASE);
        assertThat(EventAction.valueOf("REGISTRATION"))
            .isEqualTo(EventAction.REGISTRATION);
        assertThat(EventAction.valueOf("EXPIRATION"))
            .isEqualTo(EventAction.EXPIRATION);
    }
    
    @Test
    public void testValueOf_InvalidName_ThrowsException() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> EventAction.valueOf("INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testTypeFieldImmutable() {
        EventAction action = EventAction.REGISTRATION;
        String originalType = action.type;
        
        assertThat(action.type).isSameAs(originalType);
        assertThat(action.type).isEqualTo("registration");
    }
    
    @Test
    public void testEnumOrdinals() {
        assertThat(EventAction.LAST_UPDATE_OF_RDAP_DATABASE.ordinal()).isEqualTo(0);
        assertThat(EventAction.REGISTRATION.ordinal()).isEqualTo(1);
        assertThat(EventAction.EXPIRATION.ordinal()).isEqualTo(2);
    }
    
    @Test
    public void testEnumEquality() {
        EventAction action1 = EventAction.REGISTRATION;
        EventAction action2 = EventAction.valueOf("REGISTRATION");
        
        assertThat(action1).isEqualTo(action2);
        assertThat(action1).isSameAs(action2);
    }
    
    @Test
    public void testTypeFieldUnique() {
        assertThat(EventAction.LAST_UPDATE_OF_RDAP_DATABASE.type)
            .isNotEqualTo(EventAction.REGISTRATION.type);
        assertThat(EventAction.REGISTRATION.type)
            .isNotEqualTo(EventAction.EXPIRATION.type);
        assertThat(EventAction.EXPIRATION.type)
            .isNotEqualTo(EventAction.LAST_UPDATE_OF_RDAP_DATABASE.type);
    }
}