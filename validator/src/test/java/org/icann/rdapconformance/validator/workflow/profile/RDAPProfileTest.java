package org.icann.rdapconformance.validator.workflow.profile;

import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class RDAPProfileTest {

    @Test
    public void testConstructor_SingleList() {
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);
        List<ProfileValidation> validations = List.of(validation1, validation2);

        RDAPProfile profile = new RDAPProfile(validations);

        assertThat(profile).isNotNull();
    }

    @Test
    public void testConstructor_WithValidations() {
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);

        List<ProfileValidation> validations = List.of(validation1, validation2);

        RDAPProfile profile = new RDAPProfile(validations);

        assertThat(profile).isNotNull();
    }

    @Test
    public void testValidate_AllValidationsPass() {
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);

        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(true);

        List<ProfileValidation> validations = List.of(validation1, validation2);
        RDAPProfile profile = new RDAPProfile(validations);

        boolean result = profile.validate();

        assertThat(result).isTrue();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testValidate_OneValidationFails() {
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);

        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation1.validate()).thenReturn(true);
        when(validation2.validate()).thenReturn(false);

        List<ProfileValidation> validations = List.of(validation1, validation2);
        RDAPProfile profile = new RDAPProfile(validations);

        boolean result = profile.validate();

        assertThat(result).isFalse();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }

    @Test
    public void testValidate_EmptyValidationsList() {
        List<ProfileValidation> validations = List.of();
        RDAPProfile profile = new RDAPProfile(validations);

        boolean result = profile.validate();

        assertThat(result).isTrue(); // Empty list should return true
    }

    @Test
    public void testValidate_AllValidationsFail() {
        ProfileValidation validation1 = mock(ProfileValidation.class);
        ProfileValidation validation2 = mock(ProfileValidation.class);

        when(validation1.getGroupName()).thenReturn("Validation1");
        when(validation2.getGroupName()).thenReturn("Validation2");
        when(validation1.validate()).thenReturn(false);
        when(validation2.validate()).thenReturn(false);

        List<ProfileValidation> validations = List.of(validation1, validation2);
        RDAPProfile profile = new RDAPProfile(validations);

        boolean result = profile.validate();

        assertThat(result).isFalse();
        verify(validation1, times(1)).validate();
        verify(validation2, times(1)).validate();
    }
}